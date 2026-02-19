package com.ecommerce_backend.Payloads.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class StripePaymentRequestDTO {
    private Long amount;
    private String currency;
    private Long addressId;
    private Map<String, String> metaData;
}
