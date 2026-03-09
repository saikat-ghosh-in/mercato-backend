package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentConfirmationResponseDTO {
    private final boolean success;
    private final String message;
    private final String orderId;
    private final BigDecimal amount;
    private final String paymentStatus;
    private final String pgPaymentId;
}
