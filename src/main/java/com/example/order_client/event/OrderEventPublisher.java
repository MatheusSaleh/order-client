package com.example.order_client.event;

import com.example.order_client.config.RabbitConfig;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Os eventos do proprio order-client tambem passam pelo RabbitMQ, assim todos os eventos chegam
 * ao front-end pelo mesmo caminho e na ordem em que foram publicados.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

  private static final String SERVICE = "order-client";

  private final RabbitTemplate rabbitTemplate;

  public void publish(String orderId, String stage, String message, Map<String, Object> data) {
    OrderEvent event =
        new OrderEvent(orderId, stage, SERVICE, System.currentTimeMillis(), message, data);
    try {
      rabbitTemplate.convertAndSend(
          RabbitConfig.EVENTS_EXCHANGE, "order." + stage.toLowerCase(), event);
    } catch (AmqpException e) {
      log.warn("Nao foi possivel publicar o evento {} do pedido {}: {}", stage, orderId, e.getMessage());
    }
  }
}
