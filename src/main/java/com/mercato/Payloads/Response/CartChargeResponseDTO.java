package com.mercato.Payloads.Response;

import com.mercato.Entity.cart.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartChargeResponseDTO {
    private ChargeType type;
    private BigDecimal amount;
    private String description;
}
