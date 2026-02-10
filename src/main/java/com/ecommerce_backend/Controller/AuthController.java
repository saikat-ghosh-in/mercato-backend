package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.SignupRequest;
import com.ecommerce_backend.Security.payloads.UserInfoResponse;
import com.ecommerce_backend.Security.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            return authService.authenticateUser(loginRequest);
        } catch (AuthenticationException e) {
            Map<String, Object> errorMap = Map.of(
                    "message", e.getMessage(),
                    "status", false
            );
            return new ResponseEntity<>(errorMap, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            String response = authService.registerNewUser(signUpRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsernameFromAuthentication(Authentication authentication) {
        String username = authService.getCurrentUsernameFromAuthentication();
        return ResponseEntity.ok(username);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUserFromAuthentication() {
        UserInfoResponse username = authService.getCurrentUserFromAuthentication();
        return ResponseEntity.ok(username);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutCurrentUser() {
        return authService.signOutCurrentUser();
    }

    @GetMapping("/addDummyUsers")
    public String addDummyUsers() {
        return authService.addDummyUsers();
    }
}
