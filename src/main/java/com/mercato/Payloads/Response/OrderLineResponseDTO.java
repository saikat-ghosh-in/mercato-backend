package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class OrderLineResponseDTO {
    private String orderId;
    private int orderLineNumber;
    private String orderLineStatus;

    private ProductDetails product;
    private Seller seller;

    private int orderedQty;
    private int acceptedQty;
    private int shippedQty;
    private int cancelledQty;
    private int pendingQty;

    private BigDecimal lineTotal;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @AllArgsConstructor
    public static class ProductDetails {
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
    }

    @Getter
    @AllArgsConstructor
    public static class Seller {
        private String name;
        private String email;
    }
}
