package com.mercato.Payloads.Response;

import com.mercato.Entity.cart.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartChargeResponseDTO {
    private final ChargeType type;
    private final BigDecimal amount;
    private final String description;
}
