package com.mercato.Controller;

import com.mercato.Security.payloads.LoginRequest;
import com.mercato.Security.payloads.RegisterUserRequest;
import com.mercato.Security.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return authService.authenticateUser(loginRequest, request, response);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerNewUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        String response = authService.registerNewUser(registerUserRequest);
        return ResponseEntity.ok(response);
    }

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
