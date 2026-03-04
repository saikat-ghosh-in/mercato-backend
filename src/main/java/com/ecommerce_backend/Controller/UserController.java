package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.Response.EcommUserResponseDTO;
import com.ecommerce_backend.Security.services.AuthService;
import com.ecommerce_backend.Service.UserService;
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

    @GetMapping("/users/username")
    public ResponseEntity<String> getUsernameFromAuthentication(Authentication authentication) {
        String username = authService.getCurrentUsernameFromAuthentication();
        return ResponseEntity.ok(username);
    }

    @GetMapping("/users/user")
    public ResponseEntity<EcommUserResponseDTO> getCurrentUserFromAuthentication() {
        EcommUserResponseDTO user = authService.getCurrentUserFromAuthentication();
        return ResponseEntity.ok(user);
    }

//    @PutMapping("/users/user/{userId}")
//    public ResponseEntity<EcommUserResponseDTO> editUserInfo(@PathVariable String userId,
//                                                             @RequestBody Ecomm) {
//        EcommUserResponseDTO user = authService.getCurrentUserFromAuthentication();
//        return ResponseEntity.ok(user);
//    }
}
