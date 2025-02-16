package com.example.gateway.configuration;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtGatewayFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Obtener el token JWT de la cabecera Authorization
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Si hay un token, se añade a la cabecera hacia el backend
        if (token != null && token.startsWith("Bearer ")) {
            exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .build();
        }

        return chain.filter(exchange);
    }
}
