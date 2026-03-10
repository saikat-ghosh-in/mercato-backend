package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderPlacementResponseDTO {
    private final String clientSecret;
    private final OrderResponseDTO order;
}
