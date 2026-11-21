package com.example.order_client.config;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * O order-client assina todos os eventos de pedidos ({@code order.#}) com uma fila exclusiva e
 * temporaria: cada instancia recebe uma copia de todos os eventos (fan-out).
 */
@Configuration
public class RabbitConfig {

  public static final String EVENTS_EXCHANGE = "order.events";

  @Bean
  MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
    return new JacksonJsonMessageConverter(jsonMapper);
  }

  @Bean
  TopicExchange orderEventsExchange() {
    return new TopicExchange(EVENTS_EXCHANGE, true, false);
  }

  @Bean
  Queue orderEventsQueue() {
    return new AnonymousQueue();
  }

  @Bean
  Binding orderEventsBinding(Queue orderEventsQueue, TopicExchange orderEventsExchange) {
    return BindingBuilder.bind(orderEventsQueue).to(orderEventsExchange).with("order.#");
  }
}
