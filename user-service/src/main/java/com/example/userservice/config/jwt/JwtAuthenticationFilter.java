package com.example.userservice.config.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
@
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; // Asegúrate de tener la clase JwtUtil para validar y extraer datos del token

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extrae el token de la cabecera Authorization
        String token = getTokenFromRequest(request);

        // Si el token es válido, configura la autenticación en el contexto
        if (token != null && jwtUtil.isValidToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continua con el siguiente filtro de la cadena
        filterChain.doFilter(request, response);
    }

    // Método para extraer el token JWT desde la cabecera Authorization
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Elimina el prefijo "Bearer "
        }
        return null;
    }
}
