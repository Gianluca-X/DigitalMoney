package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.authservice.dto.UserEntry;
import com.example.authservice.entity.User;
import com.example.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Registro de un usuario
    @Operation(summary = "Registro de usuario", description = "Registra un nuevo usuario en el sistema")

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserEntry userEntry) {
        String token = authService.register(userEntry);
        return ResponseEntity.ok(token);
    }

    // Login de un usuario
    @Operation(summary = "Login de usuario", description = "login de usuario")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest user) {
        String token = authService.login(user);
        return ResponseEntity.ok(token);
    }

    // Cambio de email
    @Operation(summary = "cambiar email",description = "cambia el correo")
    @PatchMapping("/change-email")
    public ResponseEntity<String> changeEmail(
            @RequestParam String newEmail,
            Authentication authentication
    ) {
        String currentEmail = authentication.getName(); // viene del token (subject)
        authService.changeEmail( newEmail);
        return ResponseEntity.ok("Email updated successfully");
    }

    // Cambio de contraseña
    @Operation(summary = "cambia la contraseña",description = "cambio de contraseña")
    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestParam String newPassword) {
        authService.changePassword(newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }
    @Operation(summary = "verificar correo",description = "verifica su correo")
    @PatchMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String code) {
        authService.verifyEmail(code);
        return ResponseEntity.ok("Email verified successfully");
    }

}
