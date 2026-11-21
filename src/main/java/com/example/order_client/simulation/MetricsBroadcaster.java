package com.example.order_client.simulation;

import com.example.order_client.event.EventStreamService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A cada segundo envia ao front-end o estado real do broker (profundidade da fila e da DLQ) e do
 * worker. Tambem funciona como heartbeat da conexao SSE.
 */
@Component
@RequiredArgsConstructor
public class MetricsBroadcaster {

  private final AmqpAdmin amqpAdmin;
  private final WorkerClient workerClient;
  private final EventStreamService stream;

  @Value("${order.queue}")
  private String queue;

  @Scheduled(fixedRate = 1000)
  public void broadcast() {
    if (!stream.hasSubscribers()) {
      return;
    }
    Map<String, Object> metrics = new LinkedHashMap<>();
    metrics.put("timestamp", System.currentTimeMillis());
    metrics.put("queueDepth", depth(queue));
    metrics.put("dlqDepth", depth(queue + ".dlq"));
    metrics.put("worker", workerClient.status().orElse(null));
    stream.broadcast("metrics", metrics);
  }

  private long depth(String queueName) {
    try {
      QueueInformation info = amqpAdmin.getQueueInfo(queueName);
      return info == null ? 0 : info.getMessageCount();
    } catch (RuntimeException e) {
      return -1;
    }
  }
}
