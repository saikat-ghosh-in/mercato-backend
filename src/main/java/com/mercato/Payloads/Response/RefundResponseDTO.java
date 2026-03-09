package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class RefundResponseDTO {
    private final String gatewayReference;
    private final BigDecimal amount;
    private final String currency;
    private final String status;
    private final String reason;
    private final String failureReason;
    private final Instant createdAt;
}