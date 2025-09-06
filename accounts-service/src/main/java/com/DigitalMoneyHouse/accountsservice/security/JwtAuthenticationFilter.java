package com.DigitalMoneyHouse.accountsservice.security;

import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.service.impl.AccountsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private AccountsServiceImpl accountsServiceImpl;

    // ✅ Evitar que este filtro corra en /accounts/create
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/accounts/create");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("➡️ Request entrante: " + requestURI);

        // ✅ Si ya hay autenticación (ej: InternalTokenFilter), no procesar más
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.info("⚡ Ya existe autenticación en el contexto, salto JwtAuthenticationFilter.");
            chain.doFilter(request, response);
            return;
        }

        // Extraer token de la cabecera
        String token = extractToken(request);
        if (token == null) {
            log.error("❌ Token no presente en la cabecera Authorization");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no presente");
            return;
        }

        // Validar token
        if (!validateToken(token)) {
            log.error("❌ Token inválido");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        // Extraer email del token
        String email = extractEmailFromToken(token);
        if (email == null) {
            log.error("❌ No se pudo extraer el email del token");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        // Verificar que el usuario exista en la base
        Account account = accountsServiceImpl.findByEmail(email);
        if (account != null && email.equals(account.getEmail())) {
            log.info("✅ Autenticación exitosa para: " + email);

            // Setear autenticación en el contexto de Spring
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(account, null, new ArrayList<>())
            );
        } else {
            log.error("❌ Autenticación fallida: email no coincide o cuenta no encontrada.");
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
            log.error("Error al validar token: " + e.getMessage());
            return false;
        }
    }

    private String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject(); // el "sub" es el email
        } catch (Exception e) {
            log.error("Error al extraer email del token: " + e.getMessage());
            return null;
        }
    }
}
