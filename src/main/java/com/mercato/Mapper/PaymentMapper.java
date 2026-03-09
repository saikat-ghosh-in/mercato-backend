package com.mercato.Mapper;

import com.mercato.Entity.fulfillment.payment.Payment;
import com.mercato.Payloads.Response.PaymentResponseDTO;

public class PaymentMapper {

    public static PaymentResponseDTO toDto(Payment payment) {
        return new PaymentResponseDTO(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod().toString(),
                payment.getStatus().toString(),
                payment.getGatewayReference(),
                payment.getGatewayName(),
                payment.getGatewayResponseMessage(),
                payment.getInitiatedAt(),
                payment.getCompletedAt()
        );
    }
}
