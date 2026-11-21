package com.example.order_client.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderRequestDtoValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private static OrderRequestDto valid() {
    OrderRequestDto dto = new OrderRequestDto();
    dto.setCustomerName("Ana Souza");
    dto.setItems(List.of(new OrderItemDto("kb-01", "Teclado Mecanico", 1, 389.9)));
    return dto;
  }

  @Test
  void aceitaPedidoValido() {
    assertThat(validator.validate(valid())).isEmpty();
  }

  @Test
  void rejeitaHtmlEQuebraDeLinhaNoNome() {
    OrderRequestDto dto = valid();
    dto.setCustomerName("<img src=x onerror=alert(1)>");
    assertThat(validator.validate(dto)).isNotEmpty();

    dto.setCustomerName("Ana\nINFO falso no log");
    assertThat(validator.validate(dto)).isNotEmpty();
  }

  @Test
  void rejeitaOrderIdArbitrario() {
    OrderRequestDto dto = valid();
    dto.setOrderId("pedido-de-outra-pessoa");
    assertThat(validator.validate(dto)).isNotEmpty();
  }

  @Test
  void rejeitaPedidosGrandesOuValoresAbsurdos() {
    OrderRequestDto many = valid();
    List<OrderItemDto> items = new ArrayList<>();
    for (int i = 0; i < 21; i++) items.add(new OrderItemDto("p" + i, "x", 1, 1));
    many.setItems(items);
    assertThat(validator.validate(many)).isNotEmpty();

    OrderRequestDto expensive = valid();
    expensive.setItems(List.of(new OrderItemDto("p1", "x", 1000, -1)));
    assertThat(validator.validate(expensive)).hasSizeGreaterThanOrEqualTo(2);
  }
}
