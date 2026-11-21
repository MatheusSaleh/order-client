package com.example.order_client.config;

import br.com.exemplo.grpc.OrderServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

  /** Canal configurado em {@code spring.grpc.client.channels.order-service}. */
  @Bean
  OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub(GrpcChannelFactory channels) {
    return OrderServiceGrpc.newBlockingStub(channels.createChannel("order-service"));
  }
}
