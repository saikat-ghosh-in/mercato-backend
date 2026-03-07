package com.mercato.Payloads.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCaptureRequestDTO {

    @NotNull(message = "addressId is required")
    private String addressId;

    @NotNull(message = "paymentMethod is required")
    @Pattern(
            regexp = "UPI|CARD|NET_BANKING|WALLET|COD",
            message = "paymentMethod must be one of: UPI, CARD, NET_BANKING, WALLET, COD"
    )
    private String paymentMethod;
}