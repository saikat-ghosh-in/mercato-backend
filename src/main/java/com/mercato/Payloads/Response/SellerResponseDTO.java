package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SellerResponseDTO {
    private final String email;
    private final boolean emailVerified;
    private final Instant createdAt;
    private final String sellerDisplayName;
}
