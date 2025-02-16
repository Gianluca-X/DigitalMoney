package com.DigitalMoneyHouse.accountsservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TokenManager {
    @Value("${jwt.secret}")
    private String signingKey;
    private final long TOKEN_VALIDITY = 86400000; // 24 horas

    public String createToken(Long id, String userEmail) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", id);

        return Jwts.builder()
                .setClaims(payload)
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, signingKey)
                .compact();
    }

    public String verifyToken(String jwt) {
        try {
            Claims jwtClaims = Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(jwt)
                    .getBody();

            return jwtClaims.getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public String extractEmail(String jwt) {
        Claims jwtClaims = Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(jwt)
                .getBody();
        return jwtClaims.getSubject();
    }

    private final Set<String> revokedTokens = Collections.synchronizedSet(new HashSet<>());

    public void revokeToken(String jwt) {
        revokedTokens.add(jwt);
        System.out.println(revokedTokens);
    }
}


