package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponseDTO {
    private final String cartId;
    private final BigDecimal subtotal;
    private final List<CartChargeResponseDTO> charges;
    private final BigDecimal totalCharges;
    private final BigDecimal total;
    private final List<CartItemResponseDTO> cartItems;
}
