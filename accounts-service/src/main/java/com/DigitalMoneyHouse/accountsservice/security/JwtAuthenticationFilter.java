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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private AccountsServiceImpl accountsServiceImpl;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/accounts/create");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("➡️ Request entrante: {}", requestURI);

        String token = extractToken(request);
        if (token == null) {
            log.error("❌ Token no presente");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no presente");
            return;
        }

        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("❌ Error validando token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        String email = claims.getSubject();
        if (email == null) {
            log.error("❌ Token sin subject (email)");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }
        List<String> rolesFromToken = null;

// 1) Si viene como lista ["ADMIN"]
        if (claims.get("roles") != null) {
            rolesFromToken = claims.get("roles", List.class);
        }

// 2) Si viene como string único "ADMIN"
        if (rolesFromToken == null && claims.get("role") != null) {
            String singleRole = claims.get("role", String.class);
            rolesFromToken = List.of(singleRole);
        }

// 3) Fallback
        if (rolesFromToken == null) {
            log.warn("⚠️ Token sin roles. Se asignará ROLE_USER por defecto");
            rolesFromToken = List.of("USER");
        }

        // Convertir a autoridades de Spring Security
        List<SimpleGrantedAuthority> authorities =
                rolesFromToken.stream()
                        .map(role -> {
                            log.info("➡️ Rol detectado en token: {}", role);
                            return new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                        })
                        .collect(Collectors.toList());

        // Buscar cuenta
        Account account = accountsServiceImpl.findByEmail(email);

        if (account == null) {
            log.error("❌ Cuenta no encontrada para {}", email);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Cuenta no encontrada");
            return;
        }

        log.info("✅ Autenticación exitosa para: {} con roles: {}", email, authorities);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(account, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
