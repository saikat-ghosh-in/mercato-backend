package com.mercato.Utils;

import com.mercato.Entity.EcommUser;
import com.mercato.Entity.fulfillment.TransitionTrigger;
import com.mercato.Security.services.AuthService;
import com.mercato.Security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final AuthService authService;
    private final UserDetailsServiceImpl userDetailsService;

    public EcommUser getLoggedInUser() {
        String currentUsername = authService.getCurrentUsernameFromAuthentication();
        if (currentUsername == null) return null;
        return userDetailsService.getEcommUserByUsername(currentUsername);
    }

    public TransitionTrigger resolveTransitionTrigger() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return TransitionTrigger.ADMIN;
        }
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER"))) {
            return TransitionTrigger.SELLER;
        }
        return TransitionTrigger.CUSTOMER;
    }
}
