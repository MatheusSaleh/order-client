package com.example.order_client.event;

import java.util.Map;

/** Evento de ciclo de vida de um pedido (mesmo formato publicado por server e worker). */
public record OrderEvent(
    String orderId,
    String stage,
    String service,
    long timestamp,
    String message,
    Map<String, Object> data) {}
