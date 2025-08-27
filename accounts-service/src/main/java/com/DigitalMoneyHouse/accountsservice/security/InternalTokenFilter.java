package com.DigitalMoneyHouse.accountsservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    @Value("${internal.token}")
    private String internalToken;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Filtra solo la ruta de creación de cuentas
        String path = request.getRequestURI();
        return !path.startsWith("/accounts/create");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token != null && token.equals("Bearer " + internalToken)) {
            // Crear una autenticación "fake" para Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("internal-service", null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Continuar con la request
            filterChain.doFilter(request, response);
            return;
        }

        // Si no es token interno, seguir normalmente (Spring Security manejará JWT)
        filterChain.doFilter(request, response);
    }
}
