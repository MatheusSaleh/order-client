package com.example.order_client.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,40}$")
    private String productId;

    @Pattern(regexp = "^[\\p{L}\\p{N} .,'\"()/+&-]{0,80}$")
    private String productName;

    @Min(1)
    @Max(99)
    private int quantity;

    @PositiveOrZero
    @DecimalMax("100000")
    private double price;
}
