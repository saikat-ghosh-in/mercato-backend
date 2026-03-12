package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CashfreeOrderResponse {
    private final String cfOrderId;
    private final String paymentSessionId;
}
