//package com.DigitalMoneyHouse.accountsservice.security;
//
//import com.DigitalMoneyHouse.accountsservice.entities.Account;
//import com.DigitalMoneyHouse.accountsservice.service.impl.AccountsServiceImpl;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import java.io.IOException;
//import java.util.ArrayList;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Autowired
//    private AccountsServiceImpl accountsServiceImpl;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        String requestURI = request.getRequestURI();
//        String internalHeader = request.getHeader("X-Internal-Request");
//        logger.info(requestURI);
//        logger.info("flag request 111" + request);
//        // Permitir la solicitud si el header está presente y es "true"
////        if (requestURI.startsWith("/accounts/create") && "true".equals(internalHeader)) {
////            logger.info("Solicitud interna detectada a /accounts/create, permitida sin token.");
////            chain.doFilter(request, response);
////            return;
////        }
//
//        // Validación del token
//        String token = extractToken(request);
//        if (token == null) {
//            logger.error("Token no presente en la cabecera Authorization"+ request);
//            logger.info(request);
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no presente");
//            return;
//        }
//        if (!validateToken(token)) {
//            logger.error("❌ Token inválido.");
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido.");
//            return;
//        }
//
//        if (validateToken(token)) {
//            String email = extractEmailFromToken(token);
//            Account account = accountsServiceImpl.findByEmail(email);
//
//            if (account != null && email.equals(account.getEmail())) {
//                logger.info("Autenticación exitosa para: " + email);
//                SecurityContextHolder.getContext().setAuthentication(
//                        new UsernamePasswordAuthenticationToken(account, null, new ArrayList<>()));
//            } else {
//                logger.error("Autenticación fallida: email no coincide o cuenta no encontrada.");
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Autenticación fallida.");
//                return;
//            }
//        } else {
//            logger.error("Token inválido.");
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido.");
//            return;
//        }
//
//        chain.doFilter(request, response);
//    }
//
//
//
//    private String extractToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7); // Eliminar "Bearer " del token
//        }
//        return null;
//    }
//
//
//    private boolean validateToken(String token) {
//        // Lógica para validar el token (verificar firma, expiración, etc.)
//        try {
//            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public String extractEmailFromToken(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(secretKey) // Usa tu clave secreta
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            return claims.getSubject(); // Aquí deberías obtener el email, si está en el campo 'sub'
//        } catch (Exception e) {
//            logger.error("No se pudo extraer el email del token.", e);
//            return null;
//        }
//    }
//
//}
//
//
