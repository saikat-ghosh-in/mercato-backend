package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StateTransitionResponseDTO {
    private final String fromStatus;
    private final String toStatus;
    private final String action;
    private final String triggeredBy;
    private final Integer qtyAffected;
    private final String reason;
    private final Instant occurredAt;
}