package com.example.authservice.controller;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.UserUpdateRequest;
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
    public ResponseEntity<AuthResponse> register(@RequestBody UserEntry userEntry) {
        AuthResponse authResponse = authService.register(userEntry);
        return ResponseEntity.ok(authResponse);
    }

        // Login de un usuario
        @Operation(summary = "Login de usuario", description = "login de usuario")
        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest user) {
            AuthResponse authResponse = authService.login(user);
            return ResponseEntity.ok(authResponse);
        }

    // Cambio de email
    @Operation(summary = "actualizar usuario desde userservice", description = "actualiza correo o role")
    @PutMapping("/update")
    public ResponseEntity<AuthResponse> updateUser(
            @RequestBody UserUpdateRequest userEntry){
       AuthResponse authResponse = authService.updateUser(userEntry);
        return ResponseEntity.ok(authResponse);
    }
    @Operation(summary = "cambiar email",description = "cambia el correo")
    @PatchMapping("/change-email")
    public ResponseEntity<String> changeEmail(
            @RequestParam String newEmail,
            Authentication authentication
    ) {
        String currentEmail = authentication.getName(); // viene del token (subject)
        authService.changeEmail(newEmail);
        return ResponseEntity.ok("Email updated successfully");
    }

    // Cambio de contraseña
    @Operation(summary = "cambia la contraseña",description = "cambio de contraseña")
    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestParam String newPassword) {
        authService.changePassword(newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String code) {
        try {
            authService.verifyEmail(code);
            return ResponseEntity.ok("✅ Email verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/delete/{authId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long authId) {
        authService.deleteUser(authId);
        return ResponseEntity.ok("Usuario eliminado con éxito");
    }




}
