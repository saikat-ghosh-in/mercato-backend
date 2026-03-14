package com.mercato.Controller;

import com.mercato.Payloads.Response.EcommUserResponseDTO;
import com.mercato.Security.services.AuthService;
import com.mercato.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/admin/users")
    public ResponseEntity<List<EcommUserResponseDTO>> getAllUsers() {
        List<EcommUserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/public/sellers")
    public ResponseEntity<List<EcommUserResponseDTO>> getAllSellers() {
        List<EcommUserResponseDTO> users = userService.getAllSellers();
        return ResponseEntity.ok(users);
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
}
