package com.ecommerce_backend.Payloads.Response;

import com.ecommerce_backend.Entity.ChargeType;
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
