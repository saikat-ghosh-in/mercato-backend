package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RevenueStats {
    private final BigDecimal allTime;
    private final BigDecimal thisMonth;
    private final BigDecimal today;
    private final BigDecimal averagePerOrder;
}