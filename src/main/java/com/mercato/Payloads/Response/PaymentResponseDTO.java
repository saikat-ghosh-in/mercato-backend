package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class PaymentResponseDTO {
    private final String paymentId;
    private final BigDecimal amount;
    private final String currency;
    private final String paymentMethod;
    private final String status;
    private final String gatewayReference;
    private final String gatewayName;
    private final String gatewayResponseMessage;
    private final Instant initiatedAt;
    private final Instant completedAt;
}
