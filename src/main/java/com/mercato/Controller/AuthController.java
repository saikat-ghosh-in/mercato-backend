package com.mercato.Controller;

import com.mercato.Security.payloads.LoginRequest;
import com.mercato.Security.payloads.RegisterUserRequest;
import com.mercato.Security.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        try {
            return authService.authenticateUser(loginRequest, request, response);
        } catch (AuthenticationException e) {
            Map<String, Object> errorMap = Map.of(
                    "message", e.getMessage(),
                    "status", false
            );
            return new ResponseEntity<>(errorMap, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerNewUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        try {
            String response = authService.registerNewUser(registerUserRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{userId}/roles")
    public ResponseEntity<String> updateRoles(@PathVariable String userId,
                                              @RequestBody Set<String> roles) {

        authService.updateUserRoles(userId, roles);
        return ResponseEntity.ok("Roles modified successfully");
    }

    @PostMapping("/auth/signout")
    public ResponseEntity<?> signOutCurrentUser() {
        return authService.signOutCurrentUser();
    }
}
