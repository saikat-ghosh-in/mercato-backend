package com.mercato.Security.jwt;

import com.mercato.Security.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookieName;

    private static final String GUEST_TOKEN_COOKIE = "guest_token";
    private static final long GUEST_TOKEN_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
        return ResponseCookie.from(jwtCookieName, jwt)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }

    public ResponseCookie generateGuestTokenCookie() {
        String token = Jwts.builder()
                .subject("guest")
                .claim("type", "guest")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + GUEST_TOKEN_MAX_AGE_MS))
                .signWith(key())
                .compact();
        return ResponseCookie.from(GUEST_TOKEN_COOKIE, token)
                .path("/")
                .maxAge(GUEST_TOKEN_MAX_AGE_MS / 1000)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }

    public String extractJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return getJwtFromCookie(request);
    }

    public static String extractGuestToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, GUEST_TOKEN_COOKIE);
        return cookie != null ? cookie.getValue() : null;
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        return cookie != null ? cookie.getValue() : null;
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookieName, null)
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }

    public ResponseCookie getCleanGuestTokenCookie() {
        return ResponseCookie.from(GUEST_TOKEN_COOKIE, null)
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long getTokenExpirationTime(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().getTime();
        } catch (Exception e) {
            logger.error("Failed to get token expiration time: {}", e.getMessage());
            return 0;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getClaimFromToken(String token, String claimKey) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claimKey, String.class);
    }
}