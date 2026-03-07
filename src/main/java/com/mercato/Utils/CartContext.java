package com.mercato.Utils;

import com.mercato.Security.jwt.GuestTokenFilter;
import com.mercato.Security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record CartContext(String userId, String guestToken) {

    public static CartContext resolve(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return new CartContext(userDetails.getUserId(), null);
        }

        return new CartContext(null, GuestTokenFilter.extractGuestToken(request));
    }

    public boolean isGuest() {
        return userId == null;
    }
}
