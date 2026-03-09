package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderLineResponseDTO {
    private final String orderId;
    private final int orderLineNumber;
    private final String orderLineStatus;

    private final ProductDetails product;
    private final Seller seller;

    private final int orderedQty;
    private final int acceptedQty;
    private final int shippedQty;
    private final int cancelledQty;
    private final int pendingQty;

    private final BigDecimal lineTotal;
    private final List<StateTransitionResponseDTO> stateTransitions;

    private final Instant createdAt;
    private final Instant updatedAt;

    @Getter
    @AllArgsConstructor
    public static class ProductDetails {
        private final String productId;
        private final String productName;
        private final BigDecimal unitPrice;
    }

    @Getter
    @AllArgsConstructor
    public static class Seller {
        private final String name;
        private final String email;
    }
}
