package com.example.userservice.controller;

import com.example.userservice.dto.entry.AuthResponse;
import com.example.userservice.dto.entry.LoginRequest;
import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.entry.VerificationRequest;

import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.dto.modification.UserAliasUpdateRequest;
import com.example.userservice.service.IUserService;
import com.example.userservice.service.impl.TokenBlacklistService;
import com.example.userservice.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import java.util.Map;

@RestController
@RequestMapping("/users")
//@PreAuthorize("hasRole('client_admin')")
public class UserController {
    @Autowired
    private IUserService iuserService;
    @Autowired
    private UserServiceImpl userService;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(IUserService iuserService, TokenBlacklistService tokenBlacklistService) {
        this.iuserService = iuserService;
        this.tokenBlacklistService = tokenBlacklistService;
    }




    //@GetMapping("/search/{userName}")
    //public ResponseEntity<?> searchUserByUsername(@PathVariable String userName){
        //return ResponseEntity.ok(userService.searchUserByUsername(userName));
    //}
    @Operation(summary = "Email verificado con exito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email verificado"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUserEmail(@RequestBody VerificationRequest verificationRequest) {
        iuserService.verifyUserEmail(verificationRequest.getEmail(), verificationRequest.getVerificationCode());
        return ResponseEntity.ok("Email verified successfully.");
    }

    @Operation(summary = "Registro de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado con exito",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRegisterOutDto.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserEntryDto userEntryDto) throws IOException {
        Map<String, Object> response = iuserService.handleUserRegistration(userEntryDto);
        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully. Please check your email for the verification code.",
                "user", response));
    }
    @Operation(summary = "Usuario logueado con éxito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario logueado",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Usuario logueado con éxito"))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Llama al método `authenticateAndLogin` pasando el objeto completo `loginRequest`
        String token = iuserService.authenticateAndLogin(loginRequest);
        return ResponseEntity.ok(new AuthResponse(token));
    }
    @Operation(summary = "Logout exitoso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario deslogueado con exito"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })

    @PostMapping("/{userId}/logout")
    public ResponseEntity<String> logoutUser(@PathVariable String userId) {
        try {
            iuserService.logoutUser(userId);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during logout: " + e.getMessage());
        }

    }
    @Operation(summary = "Logout exitoso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario deslogueado con exito"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        try {
            // Elimina el prefijo "Bearer " si está presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            tokenBlacklistService.invalidateToken(token);
            return ResponseEntity.ok("Token invalidado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al invalidar el token.");
        }
    }
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserEntryDto userEntryDto){
        iuserService.updateUser(userId, userEntryDto);
        return ResponseEntity.ok("User updated successfully");
    }


    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId){
        iuserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserRegisterOutDto> getUser(@PathVariable Long id) {
        UserRegisterOutDto user = iuserServicegit add g.getUserById(id);
        return ResponseEntity.ok(user);
    }
/**
    @GetMapping("/{email}")
    public ResponseEntity<?> searchUserById(@PathVariable String email){
    return ResponseEntity.ok(userService.getUserByEmail(email));
    }
    */
@Operation(summary = "Solicitud de reseteo de password")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Solicitud enviada cone exito"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content),
        @ApiResponse(responseCode = "500", description = "Server error",
                content = @Content)
})
    @PostMapping("/request-password-reset")
public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
    iuserService.processPasswordResetRequest(email);
    return ResponseEntity.ok("Correo enviado para la recuperación de la contraseña");
    }
    @Operation(summary = "Logout exitoso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario deslogueado con exito"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                                @RequestParam String newPassword,
                                                @RequestParam String confirmPassword) {
        iuserService.resetPassword(token, newPassword, confirmPassword);
        return ResponseEntity.ok("Contraseña actualizada con éxito");
    }
    @PatchMapping("/update/alias/{id}")
    public ResponseEntity<?> updateAlias(@PathVariable Long id, @RequestBody UserAliasUpdateRequest request) {
        userService.updateAlias(id, request.getAlias());
        return ResponseEntity.ok("Alias actualizado exitosamente");
    }

}
