package com.ecommerce_backend.Security.services;

import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.SignupRequest;
import com.ecommerce_backend.Security.payloads.UserInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;

public interface AuthService {
    ResponseEntity<UserInfoResponse> authenticateUser(LoginRequest loginRequest) throws AuthenticationException;

    String registerNewUser(SignupRequest signUpRequest);

    String getCurrentUsernameFromAuthentication();

    UserInfoResponse getCurrentUserFromAuthentication();

    ResponseEntity<?> signOutCurrentUser();

    String addDummyUsers();
}
