package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class RefundResponseDTO {
    private String gatewayReference;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String reason;
    private String failureReason;
    private Instant createdAt;
}