package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartDto {

    private final Long cartId;
    private final BigDecimal subtotal;
    private final BigDecimal discountPercent;
    private final BigDecimal discountAmount;
    private final BigDecimal totalPayable;
    private final List<CartItemDto> cartItems;
}
