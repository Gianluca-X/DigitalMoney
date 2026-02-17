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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

@Component
public class InternalTokenFilter extends OncePerRequestFilter {
    private static final Logger log =
        LoggerFactory.getLogger(InternalTokenFilter.class);

    @Value("${internal.token}")
    private String internalToken;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean skip = !path.startsWith("/accounts/create");
        log.info("Should not filter for {}: {}", path, skip);
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("➡️ Request entrante a {} con Authorization={}", request.getRequestURI(), request.getHeader("Authorization"));

        String token = request.getHeader("Authorization");
        log.info("dofilterinterl");
        if (token != null && token.equals("Bearer " + internalToken)) {
            // Crear una autenticación "fake" para Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("internal-service", null, Collections.emptyList());
            log.info("entrando a token");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Continuar con la request
            filterChain.doFilter(request, response);
            return;
        }
log.info("si no esz token interno");
        // Si no es token interno, seguir normalmente (Spring Security manejará JWT)
        filterChain.doFilter(request, response);
    }
}
