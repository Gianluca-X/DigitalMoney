package com.example.userservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Enumeration;

@Slf4j
@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Obtener la solicitud HTTP actual
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("⚠ No se puede obtener la solicitud HTTP. El token no será enviado.");
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // Header opcional para indicar que es una llamada interna
            requestTemplate.header("X-Internal-Request", "true");
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
