package com.link.qwiklink.auth.jwt;

import com.link.qwiklink.auth.user.AppUserDetails;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {

    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;
    private final JwtParser parser;
    private final long expirationMillis;

    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") long expirationMillis
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.parser = Jwts.parser().verifyWith(signingKey).build();
        this.expirationMillis = expirationMillis;
    }

    /**
     * Extracts JWT from Authorization header if present.
     * @return raw token (without 'Bearer ') or null
     */
    public @Nullable String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(AUTH_HEADER);
        if (headerValue == null || !headerValue.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return headerValue.substring(TOKEN_PREFIX.length()).trim();
    }

    /**
     * Creates a signed JWT for the given authenticated principal.
     */
    public String issueToken(AppUserDetails principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        List<String> roles = principal.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Reads the subject (username) from a signed JWT.
     */
    public String getUserNameFromJwtToken(String token) {
        return parser.parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates signature + structure + expiration.
     * Returns false instead of throwing generic RuntimeException.
     */
    public boolean isTokenValid(String token) {
        try {
            parser.parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
