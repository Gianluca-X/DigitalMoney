package com.DigitalMoneyHouse.accountsservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final InternalTokenFilter internalTokenFilter;

    public SecurityConfig(InternalTokenFilter internalTokenFilter) {
        this.internalTokenFilter = internalTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs
                .authorizeHttpRequests(auth -> auth
                        // Permitir Swagger y docs abiertos
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html","/actuator/info").permitAll()
                        // Permitir endpoint de creación interna solo con token interno
                        .requestMatchers("/accounts/create").authenticated()
                        // Resto de endpoints requieren autenticación JWT
                        .anyRequest().authenticated()
                )
                // Stateless, sin sesión
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Agregar filtro de token interno antes del filtro de Spring Security
                .addFilterBefore(internalTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
