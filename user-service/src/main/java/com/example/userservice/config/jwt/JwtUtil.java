package com.example.userservice.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "miClaveSecretaSuperSegura"; // Idealmente leída desde application.yml

    // Tiempo de validez del token (ej. 1 hora)
    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    // ✅ Genera un token JWT
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // ✅ Extrae el username del token
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ Verifica si el token es válido
    public boolean isValidToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Obtiene los claims del token
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
