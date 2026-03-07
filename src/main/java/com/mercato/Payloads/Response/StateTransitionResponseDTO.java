package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StateTransitionResponseDTO {
    private String fromStatus;
    private String toStatus;
    private String action;
    private String triggeredBy;
    private Integer qtyAffected;
    private String reason;
    private Instant occurredAt;
}