package com.ecommerce_backend.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponseDTO {
    private final Long cartId;
    private final BigDecimal subtotal;
    private final List<CartItemResponseDTO> cartItems;
}
