package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentRetryResponseDTO {
    private String clientSecret;
}
