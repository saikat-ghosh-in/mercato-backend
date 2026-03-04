package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.RegisterUserRequest;
import com.ecommerce_backend.Security.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

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

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        try {
            String response = authService.registerNewUser(registerUserRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/roles")
    public ResponseEntity<String> updateRoles(@PathVariable String userId,
                                         @RequestBody Set<String> roles) {

        authService.updateUserRoles(userId, roles);
        return ResponseEntity.ok("Roles modified successfully");
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutCurrentUser() {
        return authService.signOutCurrentUser();
    }
}
