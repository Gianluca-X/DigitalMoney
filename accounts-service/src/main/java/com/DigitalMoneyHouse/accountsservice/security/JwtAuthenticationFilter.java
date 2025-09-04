package com.DigitalMoneyHouse.accountsservice.security;

import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.service.impl.AccountsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private AccountsServiceImpl accountsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.info("➡️ Request entrante: " + requestURI);

        // Extraer token de la cabecera
        String token = extractToken(request);
        if (token == null) {
            logger.error("❌ Token no presente en la cabecera Authorization");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no presente");
            return;
        }

        // Validar token
        if (!validateToken(token)) {
            logger.error("❌ Token inválido");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        // Extraer email del token
        String email = extractEmailFromToken(token);
        if (email == null) {
            logger.error("❌ No se pudo extraer el email del token");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        // Verificar que el usuario exista en la base
        Account account = accountsServiceImpl.findByEmail(email);
        if (account != null && email.equals(account.getEmail())) {
            logger.info("✅ Autenticación exitosa para: " + email);

            // Setear autenticación en el contexto de Spring
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(account, null, new ArrayList<>())
            );
        } else {
            logger.error("❌ Autenticación fallida: email no coincide o cuenta no encontrada.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Autenticación fallida");
            return;
        }

        // Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // quitar "Bearer "
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token); // si falla lanza excepción
            return true;
        } catch (Exception e) {
            logger.error("Error al validar token: " + e.getMessage());
            return false;
        }
    }

    private String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject(); // el "sub" es el email que pusiste al generarlo
        } catch (Exception e) {
            logger.error("Error al extraer email del token: " + e.getMessage());
            return null;
        }
    }
}
