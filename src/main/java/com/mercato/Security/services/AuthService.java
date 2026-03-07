package com.mercato.Security.services;

import com.mercato.Payloads.Response.EcommUserResponseDTO;
import com.mercato.Security.payloads.LoginRequest;
import com.mercato.Security.payloads.RegisterUserRequest;
import com.mercato.Security.payloads.UserInfoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;

import java.util.Set;

public interface AuthService {
    ResponseEntity<UserInfoResponse> authenticateUser(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse httpServletResponse) throws AuthenticationException;

    String registerNewUser(RegisterUserRequest registerUserRequest);

    String getCurrentUsernameFromAuthentication();

    EcommUserResponseDTO getCurrentUserFromAuthentication();

    void updateUserRoles(String userId, Set<String> roles);

    ResponseEntity<?> signOutCurrentUser();
}
