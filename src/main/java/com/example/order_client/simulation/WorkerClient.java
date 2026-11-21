package com.example.order_client.simulation;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente HTTP da API de controle do order-worker. */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerClient {

  public record WorkerStatus(
      boolean running,
      double failureRate,
      long processingDelayMs,
      int maxAttempts,
      long completed,
      long failed) {}

  private final RestClient workerRestClient;

  public Optional<WorkerStatus> status() {
    try {
      return Optional.ofNullable(
          workerRestClient.get().uri("/api/worker").retrieve().body(WorkerStatus.class));
    } catch (RestClientException e) {
      log.debug("order-worker indisponivel: {}", e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<WorkerStatus> update(Map<String, Object> changes) {
    try {
      return Optional.ofNullable(
          workerRestClient
              .put()
              .uri("/api/worker")
              .body(changes)
              .retrieve()
              .body(WorkerStatus.class));
    } catch (RestClientException e) {
      log.warn("Falha ao atualizar o order-worker: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
