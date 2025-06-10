package com.example.order_client.order.controller;

import br.com.exemplo.grpc.OrderItem;
import br.com.exemplo.grpc.OrderRequest;
import br.com.exemplo.grpc.OrderResponse;
import br.com.exemplo.grpc.OrderServiceGrpc;
import com.example.order_client.dto.OrderRequestDto;
import com.example.order_client.dto.OrderResponseDto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    @SuppressWarnings("unused")
    @GrpcClient("orderService")
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto orderRequestDto){
        OrderRequest orderRequest = OrderRequest.newBuilder()
                .setOrderId(orderRequestDto.getOrderId())
                .setCustomerName(orderRequestDto.getCustomerName())
                .addAllItems(orderRequestDto.getItems().stream().map(item ->
                        OrderItem.newBuilder()
                                .setProductId(item.getProductId())
                                .setProductName(item.getProductName())
                                .setQuantity(item.getQuantity())
                                .setPrice(item.getPrice())
                                .build()).collect(Collectors.toList()))
                .setTotalAmount(orderRequestDto.getTotalAmount())
                .setTimestamp(orderRequestDto.getTimestamp())
                .build();

        OrderResponse orderResponse = orderServiceStub.placeOrder(orderRequest);

        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setOrderId(orderResponse.getOrderId());
        responseDto.setStatus(orderResponse.getStatus());
        responseDto.setMessage(orderResponse.getMessage());

        return ResponseEntity.ok(responseDto);
    }
}
