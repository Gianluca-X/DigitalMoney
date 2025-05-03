package com.example.authservice.controller;

import com.example.authservice.dto.UserEntry;
import com.example.authservice.entity.User;
import com.example.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Registro de un usuario
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserEntry userEntry) {
        String token = authService.register(userEntry);
        return ResponseEntity.ok(token);
    }

    // Login de un usuario
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        String token = authService.login(user);
        return ResponseEntity.ok(token);
    }

    // Cambio de email
    @PatchMapping("/change-email")
    public ResponseEntity<String> changeEmail(@RequestParam Long userId, @RequestParam String newEmail) {
        authService.changeEmail(userId, newEmail);
        return ResponseEntity.ok("Email updated successfully");
    }

    // Cambio de contraseña
    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestParam Long userId, @RequestParam String newPassword) {
        authService.changePassword(userId, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }
    @PatchMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String code) {
        authService.verifyEmail(code);
        return ResponseEntity.ok("Email verified successfully");
    }

}
