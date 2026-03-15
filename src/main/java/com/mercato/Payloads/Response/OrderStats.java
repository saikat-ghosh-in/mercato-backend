package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderStats {
    private final long total;
    private final long todayCount;
    private final long pendingPayment;
    private final long activeOrders;
    private final BigDecimal averageOrderValue;
}
