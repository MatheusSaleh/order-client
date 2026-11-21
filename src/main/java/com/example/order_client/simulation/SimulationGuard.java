package com.example.order_client.simulation;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Protege a demo publica: se alguem desligar o worker ou deixar a taxa de falha alta, a
 * configuracao volta ao padrao sozinha depois de {@code order.simulation.auto-reset-seconds}
 * sem novas alteracoes. Assim um visitante nao consegue "quebrar" a demo para os proximos.
 */
@Slf4j
@Component
public class SimulationGuard {

  private final WorkerClient workerClient;
  private final Duration resetAfter;
  private final long defaultDelayMs;
  private final AtomicReference<Instant> lastChange = new AtomicReference<>(Instant.now());

  public SimulationGuard(
      WorkerClient workerClient,
      @Value("${order.simulation.auto-reset-seconds}") long resetAfterSeconds,
      @Value("${order.simulation.default-processing-delay-ms}") long defaultDelayMs) {
    this.workerClient = workerClient;
    this.resetAfter = Duration.ofSeconds(resetAfterSeconds);
    this.defaultDelayMs = defaultDelayMs;
  }

  /** Chamado a cada alteracao feita pelo simulador. */
  public void touch() {
    lastChange.set(Instant.now());
  }

  @Scheduled(fixedDelay = 10_000, initialDelay = 10_000)
  public void resetIfIdle() {
    if (resetAfter.isZero() || Instant.now().isBefore(lastChange.get().plus(resetAfter))) {
      return;
    }
    workerClient
        .status()
        .filter(s -> !s.running() || s.failureRate() > 0 || s.processingDelayMs() != defaultDelayMs)
        .ifPresent(
            s -> {
              log.info("Restaurando configuracao padrao do worker (sem alteracoes ha {}s)", resetAfter.toSeconds());
              workerClient.update(
                  Map.of("running", true, "failureRate", 0.0, "processingDelayMs", defaultDelayMs));
            });
  }
}
