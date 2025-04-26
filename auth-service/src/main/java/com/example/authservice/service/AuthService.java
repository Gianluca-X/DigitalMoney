package com.example.authservice.service;

import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public String register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return jwtUtil.generateToken(user.getUsername());
    }

    public String login(User user) {
        return userRepository.findByUsername(user.getUsername())
                .filter(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getUsername()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }
}
