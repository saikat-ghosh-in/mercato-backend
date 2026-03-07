package com.mercato.Security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Component
@Order(2)
public class GuestTokenFilter extends OncePerRequestFilter {

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookieName;

    private static final String GUEST_TOKEN_COOKIE = "guest_token";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String existingToken = extractGuestToken(request);

        if (existingToken == null && extractJwt(request) == null) {
            String newToken = UUID.randomUUID().toString();
            ResponseCookie cookie = ResponseCookie.from(GUEST_TOKEN_COOKIE, newToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        chain.doFilter(request, response);
    }

    public static String extractGuestToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> GUEST_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String extractJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        return cookie != null ? cookie.getValue() : null;
    }
}
