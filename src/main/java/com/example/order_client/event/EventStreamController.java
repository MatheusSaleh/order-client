package com.example.order_client.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Ponte entre o RabbitMQ e o navegador. O browser nao fala gRPC/AMQP, entao o order-client expoe
 * os eventos via Server-Sent Events.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventStreamController {

  private final EventStreamService stream;

  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream() {
    return stream.subscribe();
  }

  @RabbitListener(queues = "#{orderEventsQueue.name}")
  public void onOrderEvent(OrderEvent event) {
    stream.broadcast("order-event", event);
  }
}
