package com.mercato.Payloads.Response;

import com.mercato.Entity.SupplyType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplyUpdateResponseDTO {
    private String productId;
    private SupplyType supplyType;
    private int quantity;
    private boolean error;
    private String errorMessage;
}
