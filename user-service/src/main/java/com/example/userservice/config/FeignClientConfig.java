package com.example.userservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignClientConfig {

    @Value("${internal.token}")
    private String internalToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            HttpServletRequest request = getCurrentHttpRequest();
            String authHeader = null;

            if (request != null) {
                authHeader = request.getHeader("Authorization");
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // ✅ Si hay un token real del usuario → propagarlo
                log.info("Propagando token real del usuario hacia {}", requestTemplate.path());
                requestTemplate.header("Authorization", authHeader);
            } else {
                // 🛡️ Si no hay token (llamada interna) → usar internalToken
                log.info("Usando token interno para llamada Feign a {}", requestTemplate.path());
                requestTemplate.header("Authorization", "Bearer " + internalToken);
            }
        };
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

}
