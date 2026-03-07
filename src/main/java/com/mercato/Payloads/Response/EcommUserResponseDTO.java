package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class EcommUserResponseDTO {
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private boolean enabled;
    private boolean accountLocked;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant lastLoginAt;
    private boolean isSeller;
    private String sellerDisplayName;
    private List<String> roles;
}
