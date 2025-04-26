package com.example.authservice.service;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public String authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        return jwtUtil.generateToken(user.getUsername());
    }
}