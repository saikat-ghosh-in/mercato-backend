package com.ecommerce_backend.Security.services;

import com.ecommerce_backend.Payloads.Response.EcommUserResponseDTO;
import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.RegisterUserRequest;
import com.ecommerce_backend.Security.payloads.UserInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;

import java.util.Set;

public interface AuthService {
    ResponseEntity<UserInfoResponse> authenticateUser(LoginRequest loginRequest) throws AuthenticationException;

    String registerNewUser(RegisterUserRequest registerUserRequest);

    String getCurrentUsernameFromAuthentication();

    EcommUserResponseDTO getCurrentUserFromAuthentication();

    void updateUserRoles(String userId, Set<String> roles);

    ResponseEntity<?> signOutCurrentUser();
}
