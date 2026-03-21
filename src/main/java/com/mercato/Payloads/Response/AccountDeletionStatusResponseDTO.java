package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountDeletionStatusResponseDTO {
    private boolean isDeactivated;
    private long daysLeft;
}
