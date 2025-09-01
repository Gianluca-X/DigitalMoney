package com.example.userservice.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private Long authId;
    private String token; // Opcional, si quieres devolver token al frontend
}