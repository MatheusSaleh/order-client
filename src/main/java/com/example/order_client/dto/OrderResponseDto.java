package com.example.order_client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponseDto {
    private String orderId;
    private String status;
    private String message;
}
