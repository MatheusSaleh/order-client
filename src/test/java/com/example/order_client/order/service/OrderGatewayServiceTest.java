package com.example.order_client.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.exemplo.grpc.OrderRequest;
import com.example.order_client.dto.OrderItemDto;
import com.example.order_client.dto.OrderRequestDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderGatewayServiceTest {

  @Test
  void converteDtoParaProtobuf() {
    OrderRequestDto dto = new OrderRequestDto();
    dto.setOrderId("abc");
    dto.setCustomerName("Ana");
    dto.setItems(List.of(new OrderItemDto("p1", null, 2, 10.0)));

    OrderRequest proto = OrderGatewayService.toProto(dto);

    assertThat(proto.getOrderId()).isEqualTo("abc");
    assertThat(proto.getItems(0).getProductName()).isEmpty();
    assertThat(proto.getItems(0).getQuantity()).isEqualTo(2);
    assertThat(proto.getTimestamp()).isEmpty();
  }

  @Test
  void hexPreviewTruncaPayloadsGrandes() {
    assertThat(OrderGatewayService.hexPreview(new byte[] {0x0a, 0x03})).isEqualTo("0a 03");
    assertThat(OrderGatewayService.hexPreview(new byte[200])).endsWith(" ...");
  }
}
