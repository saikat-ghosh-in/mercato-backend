package com.ecommerce_backend.Security.services;

import com.ecommerce_backend.Entity.AppRole;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Role;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.Repository.RoleRepository;
import com.ecommerce_backend.Repository.UserRepository;
import com.ecommerce_backend.Security.jwt.JwtUtils;
import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.SignupRequest;
import com.ecommerce_backend.Security.payloads.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Override
    public ResponseEntity<UserInfoResponse> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        String jwt = jwtCookie.getValue();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        UserInfoResponse response = UserInfoResponse.builder()
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .token(jwt)
                .tokenExpirationTime(jwtUtils.getTokenExpirationTime(jwt))
                .roles(roles)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }

    @Override
    @Transactional
    public String registerNewUser(SignupRequest signUpRequest) {
        throwIfAnExistingUser(signUpRequest.getUsername(), signUpRequest.getEmail()); // throws

        EcommUser user = EcommUser.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .build();
        Set<Role> roles = new HashSet<>();

        if (signUpRequest.getRoles() == null) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            signUpRequest.getRoles().forEach(strRole -> {
                Role role = switch (strRole) {
                    case "admin" -> getRoleByRoleName(AppRole.ROLE_ADMIN);
                    case "seller" -> getRoleByRoleName(AppRole.ROLE_SELLER);
                    default -> getRoleByRoleName(AppRole.ROLE_USER);
                };
                roles.add(role);
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        return "User registered successfully!";
    }

    @Override
    public String getCurrentUsernameFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated())
            return authentication.getName();
        return "Guest";
    }

    @Override
    public UserInfoResponse getCurrentUserFromAuthentication() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return UserInfoResponse.builder()
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    public ResponseEntity<?> signOutCurrentUser() {
        ResponseCookie cleanCookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body("You have been signed out!");
    }


    private void throwIfAnExistingUser(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("User", "username", username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }
    }

    private Role getRoleByRoleName(AppRole appRole) {
        return roleRepository.findByRoleName(appRole)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    }
}
