package com.mercato.Mapper;

import com.mercato.Entity.fulfillment.payment.Refund;
import com.mercato.Payloads.Response.RefundResponseDTO;

public class RefundMapper {

    public static RefundResponseDTO toDto(Refund refund) {
        if (refund == null) return null;
        return new RefundResponseDTO(
                refund.getGatewayReference(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus().toString(),
                refund.getReason(),
                refund.getFailureReason(),
                refund.getCreatedAt()
        );
    }
}
