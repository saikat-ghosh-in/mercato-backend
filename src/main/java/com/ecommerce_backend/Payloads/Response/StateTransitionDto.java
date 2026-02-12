package com.ecommerce_backend.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class StateTransitionDto {
    private Long stateTransitionId;
    private String orderNumber;
    private Integer orderLineNumber;
    private String status;
    private Instant occurredAt;
    private String sellerName;
    private String sellerEmail;
    private String actorType;
    private String note;
}
