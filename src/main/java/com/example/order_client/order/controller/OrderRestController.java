package com.example.order_client.order.controller;

import com.example.order_client.dto.OrderRequestDto;
import com.example.order_client.dto.OrderResponseDto;
import com.example.order_client.dto.OrderStatusDto;
import com.example.order_client.order.service.OrderGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderGatewayService gateway;

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody OrderRequestDto request) {
        return ResponseEntity.accepted().body(gateway.placeOrder(request));
    }

    @GetMapping("/{orderId}")
    public OrderStatusDto getStatus(@PathVariable String orderId) {
        if (!orderId.matches("^[A-Za-z0-9-]{1,64}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderId invalido");
        }
        return gateway.getStatus(orderId);
    }
}
