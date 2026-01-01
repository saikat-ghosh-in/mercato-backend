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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserInfoResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles,
                jwtToken
        );
    }

    @Override
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
