package com.example.order_client.simulation;

import com.example.order_client.catalog.Catalog;
import com.example.order_client.dto.OrderItemDto;
import com.example.order_client.dto.OrderRequestDto;
import com.example.order_client.order.service.OrderGatewayService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoints do simulador: rajada de pedidos, controle do worker e redrive da DLQ.
 *
 * <p>Como sao publicos, tem limites: tamanho maximo de rajada, teto global de pedidos simulados em
 * andamento (somando todos os visitantes) e podem ser desligados com {@code SIMULATION_ENABLED=false}.
 * O nginx ainda aplica rate limit por IP.
 */
@Slf4j
@RestController
@RequestMapping("/api/simulation")
@ConditionalOnProperty(name = "order.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationController {

  private static final List<String> CUSTOMERS =
      List.of("Ana", "Bruno", "Carla", "Diego", "Elisa", "Felipe", "Gabi", "Heitor", "Isis", "Joao");

  private final OrderGatewayService gateway;
  private final WorkerClient workerClient;
  private final ExecutorService simulationExecutor;
  private final DeadLetterService deadLetterService;
  private final SimulationGuard guard;
  private final int maxBurst;
  private final Semaphore inFlight;

  public SimulationController(
      OrderGatewayService gateway,
      WorkerClient workerClient,
      ExecutorService simulationExecutor,
      DeadLetterService deadLetterService,
      SimulationGuard guard,
      @Value("${order.simulation.max-burst}") int maxBurst,
      @Value("${order.simulation.max-in-flight}") int maxInFlight) {
    this.gateway = gateway;
    this.workerClient = workerClient;
    this.simulationExecutor = simulationExecutor;
    this.deadLetterService = deadLetterService;
    this.guard = guard;
    this.maxBurst = maxBurst;
    this.inFlight = new Semaphore(maxInFlight);
  }

  public record BurstRequest(Integer count) {}

  public record WorkerUpdate(Boolean running, Double failureRate, Long processingDelayMs) {}

  @PostMapping("/burst")
  public ResponseEntity<Map<String, Object>> burst(@RequestBody(required = false) BurstRequest body) {
    int count = Math.clamp(body == null || body.count() == null ? 20 : body.count(), 1, maxBurst);
    if (!inFlight.tryAcquire(count)) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Muitos pedidos simulados em andamento. Tente em alguns segundos.");
    }
    for (int i = 0; i < count; i++) {
      long delay = i * 60L; // espalha os pedidos para a animacao ficar legivel
      simulationExecutor.submit(
          () -> {
            try {
              Thread.sleep(delay);
              gateway.placeOrder(randomOrder());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
              log.warn("Falha ao enviar pedido simulado: {}", e.getMessage());
            } finally {
              inFlight.release();
            }
          });
    }
    return ResponseEntity.accepted().body(Map.of("accepted", count));
  }

  @PostMapping("/dlq/redrive")
  public Map<String, Object> redriveDeadLetters() {
    return Map.of("requeued", deadLetterService.redrive());
  }

  @GetMapping("/worker")
  public ResponseEntity<WorkerClient.WorkerStatus> worker() {
    return workerClient
        .status()
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(503).build());
  }

  @PutMapping("/worker")
  public ResponseEntity<WorkerClient.WorkerStatus> updateWorker(@RequestBody WorkerUpdate update) {
    Map<String, Object> changes = new HashMap<>();
    if (update.running() != null) changes.put("running", update.running());
    if (update.failureRate() != null) changes.put("failureRate", Math.clamp(update.failureRate(), 0.0, 1.0));
    if (update.processingDelayMs() != null) {
      changes.put("processingDelayMs", Math.clamp(update.processingDelayMs(), 100L, 5_000L));
    }
    guard.touch();
    return workerClient
        .update(changes)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(503).build());
  }

  static OrderRequestDto randomOrder() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int itemCount = random.nextInt(1, 4);
    List<Catalog.Product> products = new ArrayList<>(Catalog.PRODUCTS);
    Collections.shuffle(products, random);

    OrderRequestDto dto = new OrderRequestDto();
    dto.setCustomerName(CUSTOMERS.get(random.nextInt(CUSTOMERS.size())));
    dto.setTimestamp(Instant.now().toString());
    dto.setItems(
        products.subList(0, itemCount).stream()
            .map(p -> new OrderItemDto(p.id(), p.name(), random.nextInt(1, 4), p.price()))
            .toList());
    return dto;
  }
}
