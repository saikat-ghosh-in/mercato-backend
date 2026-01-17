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

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        UserInfoResponse response = UserInfoResponse.builder()
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                        jwtCookie.toString())
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

    @Override
    @Transactional
    public String addDummyUsers() {
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseGet(() -> {
                    Role newUserRole = new Role(AppRole.ROLE_USER);
                    return roleRepository.save(newUserRole);
                });

        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                .orElseGet(() -> {
                    Role newSellerRole = new Role(AppRole.ROLE_SELLER);
                    return roleRepository.save(newSellerRole);
                });

        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role newAdminRole = new Role(AppRole.ROLE_ADMIN);
                    return roleRepository.save(newAdminRole);
                });

        Set<Role> userRoles = new HashSet<>(Set.of(getRoleByRoleName(AppRole.ROLE_USER)));
        Set<Role> sellerRoles = new HashSet<>(Set.of(getRoleByRoleName(AppRole.ROLE_SELLER)));
        Set<Role> adminRoles = new HashSet<>(Set.of(getRoleByRoleName(AppRole.ROLE_USER),
                getRoleByRoleName(AppRole.ROLE_SELLER),
                getRoleByRoleName(AppRole.ROLE_ADMIN)));


        // Create users if not already present
        if (!userRepository.existsByUsername("user1")) {
            EcommUser user1 = EcommUser.builder()
                    .username("user1")
                    .email("user1@example.com")
                    .password(encoder.encode("password1"))
                    .build();
            userRepository.save(user1);
        }

        if (!userRepository.existsByUsername("seller1")) {
            EcommUser seller1 = EcommUser.builder()
                    .username("seller1")
                    .email("seller1@example.com")
                    .password(encoder.encode("password2"))
                    .build();
            userRepository.save(seller1);
        }

        if (!userRepository.existsByUsername("admin")) {
            EcommUser admin = EcommUser.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(encoder.encode("adminPass"))
                    .build();
            userRepository.save(admin);
        }

        // Update roles for existing users
        userRepository.findByUsername("user1").ifPresent(user -> {
            user.setRoles(userRoles);
            userRepository.save(user);
        });

        userRepository.findByUsername("seller1").ifPresent(seller -> {
            seller.setRoles(sellerRoles);
            userRepository.save(seller);
        });

        userRepository.findByUsername("admin").ifPresent(admin -> {
            admin.setRoles(adminRoles);
            userRepository.save(admin);
        });
        return "success";
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
