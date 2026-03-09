package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponseDTO {
    private String orderId;
    private String orderStatus;

    private Customer customer;
    private DeliveryAddress deliveryAddress;
    private PaymentResponseDTO paymentSummary;
    private RefundResponseDTO refundSummary;

    private List<OrderLineResponseDTO> orderLines;

    private String currency;
    private BigDecimal subTotal;
    private BigDecimal charges;
    private BigDecimal totalAmount;

    private Instant createDate;
    private Instant updateDate;


    @Getter
    @AllArgsConstructor
    public static class Customer {
        private String name;
        private String email;
    }

    @Getter
    @AllArgsConstructor
    public static class DeliveryAddress {
        private String recipientName;
        private String recipientPhone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
    }
}
