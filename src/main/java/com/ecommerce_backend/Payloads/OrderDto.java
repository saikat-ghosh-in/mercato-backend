package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private String orderNumber;
    private String orderStatus;

    private Customer customer;
    private DeliveryAddress deliveryAddress;
    private PaymentDto paymentSummary;

    private List<OrderLineDto> orderLines;

    private String currency;
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;

    private Map<String, List<StateTransitionDto>> stateTransitions;
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
