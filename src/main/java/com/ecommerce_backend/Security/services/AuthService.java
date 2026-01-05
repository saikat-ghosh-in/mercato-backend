package com.ecommerce_backend.Security.services;

import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.SignupRequest;
import com.ecommerce_backend.Security.payloads.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    ResponseEntity<UserInfoResponse> authenticateUser(@RequestBody LoginRequest loginRequest) throws AuthenticationException;

    String registerNewUser(@Valid @RequestBody SignupRequest signUpRequest);

    String getUsernameFromAuthentication(Authentication authentication);

    UserInfoResponse getUserDetailsFromAuthentication(Authentication authentication);

    ResponseEntity<?> signOutCurrentUser();
}
