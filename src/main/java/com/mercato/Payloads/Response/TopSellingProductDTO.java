package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TopSellingProductDTO {
    private final String productId;
    private final String productName;
    private final int totalShippedQty;
    private final BigDecimal totalRevenue;
}
