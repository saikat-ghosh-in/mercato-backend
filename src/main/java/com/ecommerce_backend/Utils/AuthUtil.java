package com.ecommerce_backend.Utils;

import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Security.services.AuthService;
import com.ecommerce_backend.Security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final AuthService authService;
    private final UserDetailsServiceImpl userDetailsService;

    public EcommUser getLoggedInUser() {
        String currentUsername = authService.getCurrentUsernameFromAuthentication();
        return userDetailsService.getEcommUserByUsername(currentUsername); // throws
    }
}
