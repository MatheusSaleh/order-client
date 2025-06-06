package com.example.order_client.order.controller;

import br.com.exemplo.grpc.OrderServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    @GrpcClient("orderService")
    private  OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

}
