package com.mercato.Payloads.Response;

import com.mercato.Entity.SupplyType;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ProductSupplyUpdateResponseDTO {
    private final String productId;
    private final SupplyType supplyType;
    private final int quantity;
    private final boolean error;
    private final String errorMessage;
}
