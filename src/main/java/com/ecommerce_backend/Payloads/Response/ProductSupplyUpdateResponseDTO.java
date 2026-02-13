package com.ecommerce_backend.Payloads.Response;

import com.ecommerce_backend.Entity.SupplyType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplyUpdateResponseDTO {
    private Long productId;
    private SupplyType supplyType;
    private Integer quantity;
    private boolean error;
    private String errorMessage;
}
