package com.mercato.Payloads.Response;

import com.mercato.Entity.fulfillment.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class OrderSummaryDTO {
    private final String orderId;
    private final OrderStatus orderStatus;
    private final BigDecimal totalAmount;
    private final Instant createdAt;
}
