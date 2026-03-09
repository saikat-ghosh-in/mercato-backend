package com.mercato.Payloads.Request;

import com.mercato.Entity.SupplyType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductSupplyUpdateRequestDTO {
    private String productId;
    private SupplyType supplyType;
    private Integer quantity;
}
