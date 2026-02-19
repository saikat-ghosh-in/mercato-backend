package com.ecommerce_backend.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentConfirmationResponseDTO {
    private boolean success;
    private String message;
    private String orderNumber;
    private Long orderId;
    private BigDecimal amount;
    private String paymentStatus;
    private String pgPaymentId;
}
