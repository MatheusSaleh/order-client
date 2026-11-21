package com.example.order_client.dto;

import java.util.List;

public record OrderStatusDto(
    String orderId,
    String status,
    String customerName,
    double totalAmount,
    String lastUpdated,
    List<OrderItemDto> items) {}
