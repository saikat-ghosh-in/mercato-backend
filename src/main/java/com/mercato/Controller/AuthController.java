package com.mercato.Controller;

import com.mercato.Payloads.Response.EcommUserResponseDTO;
import com.mercato.Payloads.Response.MessageResponse;
import com.mercato.Security.payloads.LoginRequest;
import com.mercato.Security.payloads.RegisterUserRequest;
import com.mercato.Security.services.AuthService;
import com.mercato.Service.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        return authService.authenticateUser(loginRequest, request, response);
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerNewUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        String response = authService.registerNewUser(registerUserRequest);
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(new MessageResponse("Email verified successfully! Welcome to Mercato."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestParam @Email String email) {
        emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok(new MessageResponse("Verification email sent! Please check your inbox."));
    }

    @GetMapping("/user/username")
    public ResponseEntity<String> getUsernameFromAuthentication(Authentication authentication) {
        String username = authService.getCurrentUsernameFromAuthentication();
        return ResponseEntity.ok(username);
    }

    @GetMapping("/user")
    public ResponseEntity<EcommUserResponseDTO> getCurrentUserFromAuthentication() {
        EcommUserResponseDTO user = authService.getCurrentUserFromAuthentication();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutCurrentUser() {
        return authService.signOutCurrentUser();
    }
}
