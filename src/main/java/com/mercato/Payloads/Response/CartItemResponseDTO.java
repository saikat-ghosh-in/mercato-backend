package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemResponseDTO {
    private final String productId;
    private final int quantity;
    private final BigDecimal itemPrice;
    private final BigDecimal lineTotal;
    private final boolean outOfStock;
}