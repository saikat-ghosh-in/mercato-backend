package com.mercato.Payloads.Request;

import com.mercato.Entity.SupplyType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSupplyUpdateRequestDTO {
    private String productId;
    private SupplyType supplyType;
    private Integer quantity;
}
