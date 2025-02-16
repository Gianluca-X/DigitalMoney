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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Agregar log para ver si el token está presente en la cabecera
        String token = extractToken(request);
        if (token == null) {
            logger.error("Token no presente en la cabecera Authorization");
        } else {
            logger.info("Token recibido: " + token);
        }

        // Verificar que el token sea válido
        if (token != null && validateToken(token)) {
            String email = extractEmailFromToken(token);

            if (email == null) {
                logger.error("No se pudo extraer el email del token.");
            } else {
                logger.info("Email extraído del token: " + email);
            }

            // Validar si el email del token coincide con el email de la cuenta
            Account account = accountsServiceImpl.findByEmail(email);
            if (account != null && email.equals(account.getEmail())) {
                logger.info("Autenticación exitosa para el usuario con email: " + email);

                // Continuar con la autenticación
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(account, null, new ArrayList<>()));
            } else {
                logger.error("Autenticación fallida: el email del token no coincide o la cuenta no fue encontrada.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Autenticación fallida.");
                return;
            }
        }

        chain.doFilter(request, response);
    }



    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Eliminar "Bearer " del token
        }
        return null;
    }


    private boolean validateToken(String token) {
        // Lógica para validar el token (verificar firma, expiración, etc.)
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey) // Usa tu clave secreta
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // Aquí deberías obtener el email, si está en el campo 'sub'
        } catch (Exception e) {
            logger.error("No se pudo extraer el email del token.", e);
            return null;
        }
    }

}


