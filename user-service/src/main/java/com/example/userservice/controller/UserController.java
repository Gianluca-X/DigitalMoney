    package com.example.userservice.controller;
    
    import com.example.userservice.dto.entry.UserEntryDto;
    import com.example.userservice.dto.entry.UserRegisterRequest;
    import com.example.userservice.dto.exit.UserRegisterOutDto;
    import com.example.userservice.dto.modification.UserAliasUpdateRequest;
    import com.example.userservice.entity.User;
    import com.example.userservice.service.IUserService;
    import com.example.userservice.service.impl.UserServiceImpl;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.media.Content;
    import io.swagger.v3.oas.annotations.media.Schema;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.oauth2.jwt.Jwt;
    import org.springframework.web.bind.annotation.*;
    
    import java.io.IOException;
    import java.util.Map;
    
    @RestController
    @RequestMapping("/users")
    @RequiredArgsConstructor
    public class UserController {
    
        private final IUserService iuserService;
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Cuenta creada correctamente", content = @Content),
                @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
                @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
                @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
        })
        @PostMapping("/register")
        public ResponseEntity<UserRegisterOutDto> register(@Valid @RequestBody UserRegisterRequest request) {
            UserRegisterOutDto result = iuserService.handleRegister(request);
            return ResponseEntity.status(201).body(result);
        }


        @GetMapping("/{id}")
        public ResponseEntity<User> getUser(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
            return ResponseEntity.ok(iuserService.getUserById(id, jwt));
        }
    
        @PutMapping("/update/{userId}")
        public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserEntryDto dto) {
            iuserService.updateUser(userId, dto);
            return ResponseEntity.ok("Usuario actualizado");
        }
    
        @DeleteMapping("/delete/{userId}")
        public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
            iuserService.deleteUser(userId);
            return ResponseEntity.ok("Usuario eliminado con id " + userId);
        }
    
        @PatchMapping("/update/alias/{id}")
        public ResponseEntity<?> updateAlias(@PathVariable Long id, @RequestBody UserAliasUpdateRequest request) {
            iuserService.updateAlias(id, request.getAlias());
            return ResponseEntity.ok("Alias actualizado");
        }
        @PostMapping("/logout")
        public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
            try {
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.badRequest().body("Token no proporcionado");
                }

                String token = authHeader.substring(7); // quitar "Bearer "

                // Opcional: agregar token a blacklist para invalidarlo antes de que expire
                // jwtBlacklistService.add(token);

                System.out.println("🔒 Logout exitoso para token: " + token);

                return ResponseEntity.ok("Logout exitoso");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Error interno del servidor");
            }
        }

    }
