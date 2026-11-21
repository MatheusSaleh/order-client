package com.example.order_client.simulation;

import com.example.order_client.event.OrderEventPublisher;
import com.rabbitmq.client.GetResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * "Redrive" da dead letter queue: devolve os pedidos que falharam para a fila principal, para
 * serem processados novamente (ex.: depois de corrigir a causa da falha).
 */
@Service
@RequiredArgsConstructor
public class DeadLetterService {

  private static final int MAX_PER_REQUEST = 500;

  private final RabbitTemplate rabbitTemplate;
  private final JsonMapper jsonMapper;

  private record OrderIdOnly(String orderId) {}
  private final OrderEventPublisher events;

  @Value("${order.queue}")
  private String queue;

  public int redrive() {
    String dlq = queue + ".dlq";
    Integer moved =
        rabbitTemplate.execute(
            channel -> {
              int count = 0;
              GetResponse message;
              // ack manual: a mensagem so sai da DLQ depois de republicada
              while (count < MAX_PER_REQUEST && (message = channel.basicGet(dlq, false)) != null) {
                events.publish(
                    orderIdOf(message.getBody()),
                    "REQUEUED",
                    "Pedido retirado da DLQ e devolvido para a fila " + queue,
                    Map.of("from", dlq, "to", queue));
                channel.basicPublish("", queue, message.getProps(), message.getBody());
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                count++;
              }
              return count;
            });
    return moved == null ? 0 : moved;
  }

  private String orderIdOf(byte[] body) {
    try {
      String id = jsonMapper.readValue(body, OrderIdOnly.class).orderId();
      return id == null ? "desconhecido" : id;
    } catch (JacksonException e) {
      return "desconhecido";
    }
  }
}
