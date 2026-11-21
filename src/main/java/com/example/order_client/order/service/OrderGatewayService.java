package com.example.order_client.order.service;

import br.com.exemplo.grpc.GetOrderStatusRequest;
import br.com.exemplo.grpc.GetOrderStatusResponse;
import br.com.exemplo.grpc.OrderItem;
import br.com.exemplo.grpc.OrderRequest;
import br.com.exemplo.grpc.OrderResponse;
import br.com.exemplo.grpc.OrderServiceGrpc;
import com.example.order_client.dto.OrderItemDto;
import com.example.order_client.dto.OrderRequestDto;
import com.example.order_client.dto.OrderResponseDto;
import com.example.order_client.dto.OrderStatusDto;
import com.example.order_client.event.OrderEventPublisher;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Traduz REST/JSON para gRPC/Protobuf. Tambem publica os eventos das primeiras etapas do pedido,
 * incluindo a comparacao de tamanho JSON x Protobuf usada no modo didatico do front-end.
 */
@Service
@RequiredArgsConstructor
public class OrderGatewayService {

  private static final int HEX_PREVIEW_BYTES = 96;
  private static final long DEADLINE_SECONDS = 5;

  private final OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;
  private final OrderEventPublisher events;
  private final JsonMapper jsonMapper;

  public OrderResponseDto placeOrder(OrderRequestDto dto) {
    String orderId =
        dto.getOrderId() == null || dto.getOrderId().isBlank()
            ? UUID.randomUUID().toString()
            : dto.getOrderId();
    dto.setOrderId(orderId);

    byte[] json = toJson(dto);
    events.publish(
        orderId,
        "CLIENT_RECEIVED",
        "order-client recebeu POST /api/orders em JSON",
        Map.of("endpoint", "POST /api/orders", "jsonBytes", json.length));

    OrderRequest request = toProto(dto);
    byte[] protobuf = request.toByteArray();

    Map<String, Object> wire = new LinkedHashMap<>();
    wire.put("rpc", "order.OrderService/PlaceOrder");
    wire.put("jsonBytes", json.length);
    wire.put("protobufBytes", protobuf.length);
    wire.put("json", new String(json, java.nio.charset.StandardCharsets.UTF_8));
    wire.put("protobufHex", hexPreview(protobuf));
    events.publish(orderId, "GRPC_SENT", "JSON convertido em Protobuf e enviado via gRPC", wire);

    long start = System.nanoTime();
    OrderResponse response =
        orderServiceStub
            .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
            .placeOrder(request);
    long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

    events.publish(
        orderId,
        "GRPC_RESPONDED",
        "order-server respondeu " + response.getStatus() + " em " + latencyMs + " ms",
        Map.of("status", response.getStatus(), "latencyMs", latencyMs));

    OrderResponseDto result = new OrderResponseDto();
    result.setOrderId(response.getOrderId());
    result.setStatus(response.getStatus());
    result.setMessage(response.getMessage());
    return result;
  }

  public OrderStatusDto getStatus(String orderId) {
    GetOrderStatusResponse response =
        orderServiceStub
            .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
            .getOrderStatus(GetOrderStatusRequest.newBuilder().setOrderId(orderId).build());

    return new OrderStatusDto(
        response.getOrderId(),
        response.getStatus(),
        response.getCustomerName(),
        response.getTotalAmount(),
        response.getLastUpdated(),
        response.getItemsList().stream()
            .map(
                item ->
                    new OrderItemDto(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()))
            .toList());
  }

  static OrderRequest toProto(OrderRequestDto dto) {
    OrderRequest.Builder builder =
        OrderRequest.newBuilder()
            .setOrderId(dto.getOrderId())
            .setCustomerName(dto.getCustomerName())
            .setTotalAmount(dto.getTotalAmount());
    if (dto.getTimestamp() != null) {
      builder.setTimestamp(dto.getTimestamp());
    }
    dto.getItems()
        .forEach(
            item ->
                builder.addItems(
                    OrderItem.newBuilder()
                        .setProductId(item.getProductId())
                        .setProductName(item.getProductName() == null ? "" : item.getProductName())
                        .setQuantity(item.getQuantity())
                        .setPrice(item.getPrice())));
    return builder.build();
  }

  static String hexPreview(byte[] bytes) {
    int length = Math.min(bytes.length, HEX_PREVIEW_BYTES);
    String hex = HexFormat.ofDelimiter(" ").formatHex(bytes, 0, length);
    return bytes.length > length ? hex + " ..." : hex;
  }

  private byte[] toJson(OrderRequestDto dto) {
    try {
      return jsonMapper.writeValueAsBytes(dto);
    } catch (JacksonException e) {
      return new byte[0];
    }
  }
}
