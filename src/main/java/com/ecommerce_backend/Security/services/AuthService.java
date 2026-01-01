package com.ecommerce_backend.Security.services;

import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.SignupRequest;
import com.ecommerce_backend.Security.payloads.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    UserInfoResponse authenticateUser(@RequestBody LoginRequest loginRequest) throws AuthenticationException;

    String registerNewUser(@Valid @RequestBody SignupRequest signUpRequest);
}
