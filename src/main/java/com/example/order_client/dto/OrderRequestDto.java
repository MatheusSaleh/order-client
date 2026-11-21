package com.example.order_client.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/** Limites espelhados no order-server (OrderRequestValidator). */
@Data
public class OrderRequestDto {
    /** Opcional: se vier vazio, o order-client gera um UUID. */
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "deve ser um UUID")
    private String orderId;

    @NotBlank
    @Pattern(regexp = "^[\\p{L}\\p{N} .'-]{1,80}$", message = "1 a 80 letras, numeros ou espacos")
    private String customerName;

    @NotEmpty
    @Size(max = 20)
    private List<@Valid OrderItemDto> items;

    /** Opcional: se vier zerado, o order-server calcula pelos itens. */
    @PositiveOrZero
    @DecimalMax("200000000")
    private double totalAmount;

    @Pattern(regexp = "^[0-9A-Za-z:.+-]{0,40}$")
    private String timestamp;
}
