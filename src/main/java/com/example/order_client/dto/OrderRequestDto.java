package com.example.order_client.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {
    private String orderId;
    private String customerName;
    private List<OrderItemDto> items;
    private double totalAmount;
    private String timestamp;
}
