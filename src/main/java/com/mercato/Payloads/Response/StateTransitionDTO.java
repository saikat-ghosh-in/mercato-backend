package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class StateTransitionDTO {
    private Long stateTransitionId;
    private String orderId;
    private int orderLineNumber;
    private String status;
    private Instant occurredAt;
    private String sellerName;
    private String sellerEmail;
    private String actorType;
    private String note;
}
