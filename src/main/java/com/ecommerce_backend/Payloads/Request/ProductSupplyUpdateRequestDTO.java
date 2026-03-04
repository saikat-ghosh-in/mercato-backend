package com.ecommerce_backend.Payloads.Request;

import com.ecommerce_backend.Entity.SupplyType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSupplyUpdateRequestDTO {
    private String productId;
    private SupplyType supplyType;
    private Integer quantity;
}
