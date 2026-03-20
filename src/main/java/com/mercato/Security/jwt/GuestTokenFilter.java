package com.mercato.Security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GuestTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String existingToken = JwtUtils.extractGuestToken(request);

        if (jwtUtils.extractJwt(request) == null) {
            if (existingToken == null || !jwtUtils.validateGuestToken(existingToken)) {
                String newGuestToken = jwtUtils.generateGuestTokenValue();
                request.setAttribute(JwtUtils.GUEST_TOKEN_ATTRIBUTE, newGuestToken);

                ResponseCookie cookie = jwtUtils.createGuestTokenCookie(newGuestToken);
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            }
        }

        chain.doFilter(request, response);
    }
}