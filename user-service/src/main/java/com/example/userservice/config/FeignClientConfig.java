package com.example.userservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Enumeration;

@Slf4j
@Configuration
public class FeignClientConfig {

    @Value("${internal.token}")
    private String internalToken;
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            log.info("Agregando header interno para microservicio");

            requestTemplate.header("Authorization", "Bearer " + internalToken);
        };
    }


    /**
     * Convierte los headers de la petición en un string para debugging
     */
    private String getHeadersAsString(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                headers.append(header).append(": ").append(request.getHeader(header)).append(" | ");
            }
        }

        return headers.toString();
    }
}
