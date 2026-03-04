package com.ecommerce_backend.Security.services;

import com.ecommerce_backend.Entity.AppRole;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Role;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Response.EcommUserResponseDTO;
import com.ecommerce_backend.Repository.RoleRepository;
import com.ecommerce_backend.Repository.UserRepository;
import com.ecommerce_backend.Security.jwt.JwtUtils;
import com.ecommerce_backend.Security.payloads.LoginRequest;
import com.ecommerce_backend.Security.payloads.RegisterUserRequest;
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

import java.time.Instant;
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
    @Transactional
    public ResponseEntity<UserInfoResponse> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        EcommUser user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

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
    public String registerNewUser(RegisterUserRequest registerUserRequest) {
        throwIfAnExistingUser(registerUserRequest.getUsername(), registerUserRequest.getEmail()); // throws

        EcommUser user = EcommUser.builder()
                .username(registerUserRequest.getUsername())
                .email(registerUserRequest.getEmail())
                .password(encoder.encode(registerUserRequest.getPassword()))
                .firstName(registerUserRequest.getFirstName())
                .lastName(registerUserRequest.getLastName())
                .phoneNumber(registerUserRequest.getPhoneNumber())
                .profileImageUrl("placeholder")
                .enabled(true)
                .emailVerified(true)
                .build();
        Set<Role> roles = new HashSet<>();

        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);

        return "User registered successfully!";
    }

    @Override
    @Transactional
    public void updateUserRoles(String userId, Set<String> roleNames) {

        EcommUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Role> newRoles = new HashSet<>();

        for (String roleName : roleNames) {
            AppRole appRole;
            try {
                appRole = AppRole.valueOf(roleName);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + roleName);
            }

            Role role = roleRepository.findByRoleName(appRole)
                    .orElseThrow(() -> new RuntimeException("Role not found in DB: " + roleName));

            newRoles.add(role);
        }

        user.setRoles(newRoles);
    }

    @Override
    public String getCurrentUsernameFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated())
            return authentication.getName();
        return "Guest";
    }

    @Override
    public EcommUserResponseDTO getCurrentUserFromAuthentication() {

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
        EcommUser user = getUserByUserId(userDetails.getUserId());

        return new EcommUserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.isEnabled(),
                user.isAccountLocked(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.isSeller(),
                user.getSellerDisplayName(),
                roles
        );
    }

    @Override
    public ResponseEntity<?> signOutCurrentUser() {
        ResponseCookie cleanCookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body("You have been signed out!");
    }


    private EcommUser getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
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
