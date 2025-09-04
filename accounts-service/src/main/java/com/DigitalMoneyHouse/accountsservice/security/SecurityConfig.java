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
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(InternalTokenFilter internalTokenFilter, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.internalTokenFilter = internalTokenFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/info").permitAll()
                        .requestMatchers("/accounts/create").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // primero el filtro de token interno
                .addFilterBefore(internalTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // después tu filtro JWT casero
                .addFilterAfter(jwtAuthenticationFilter, InternalTokenFilter.class);

        return http.build();
    }
}

