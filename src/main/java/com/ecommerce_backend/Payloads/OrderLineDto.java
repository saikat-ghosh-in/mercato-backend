package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class OrderLineDto {

    private Long orderLineId;
    private String orderNumber;
    private Integer orderLineNumber;
    private String orderLineStatus;

    private ProductDetails product;
    private Seller seller;

    private Integer quantity;
    private BigDecimal lineTotal;
    private Instant createDate;
    private Instant updateDate;


    @Getter
    @AllArgsConstructor
    public static class ProductDetails {
        private Long productId;
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
