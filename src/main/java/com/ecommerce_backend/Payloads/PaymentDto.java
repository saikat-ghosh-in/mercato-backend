package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class PaymentDto {
    private Long paymentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String gatewayReference;
    private String gatewayName;
    private String gatewayResponseMessage;
    private Instant initiatedAt;
    private Instant completedAt;
}
