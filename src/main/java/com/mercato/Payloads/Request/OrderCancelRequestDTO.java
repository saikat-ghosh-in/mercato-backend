package com.mercato.Payloads.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCancelRequestDTO {
    @NotBlank(message = "Order ID is required")
    private String orderId;
    private String reason;
}
