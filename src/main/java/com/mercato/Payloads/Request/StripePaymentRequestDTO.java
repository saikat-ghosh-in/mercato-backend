package com.mercato.Payloads.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class StripePaymentRequestDTO {
    private Long amount;
    private String currency;
    private String addressId;
    private Map<String, String> metaData;
}
