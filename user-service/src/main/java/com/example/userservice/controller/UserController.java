package com.example.userservice.controller;

import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.entry.UserRegisterRequest;
import com.example.userservice.dto.modification.UserAliasUpdateRequest;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import com.example.userservice.exceptions.UnauthorizedException;
import com.example.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterOutDto> register(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(201).body(userService.handleRegister(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal String email,
            Authentication auth) {

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }

        checkOwnerOrAdmin(auth, id);
        return ResponseEntity.ok(userService.getUserById(id, email));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UserEntryDto dto,
            @AuthenticationPrincipal String email,
            Authentication auth) {

        checkOwnerOrAdmin(auth, userId);
        userService.updateUser(userId, dto, email);
        return ResponseEntity.ok("Usuario actualizado");
    }

    @PatchMapping("/update/alias/{id}")
    public ResponseEntity<?> updateAlias(
            @PathVariable Long id,
            @RequestBody UserAliasUpdateRequest request,
            @AuthenticationPrincipal String email,
            Authentication auth) {

        checkOwnerOrAdmin(auth, id);
        userService.updateAlias(id, request.getAlias(), email);
        return ResponseEntity.ok("Alias actualizado");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal String email,
            Authentication auth) {

        checkOwnerOrAdmin(auth, userId);
        userService.deleteUser(userId, email);
        return ResponseEntity.ok("Usuario eliminado");
    }

    public void checkOwnerOrAdmin(Authentication auth, Long targetUserId) {

        String email = auth.getPrincipal().toString();
        User user = userService.findByEmail(email);

        if (user == null) {
            throw new UnauthorizedException("Usuario no encontrado.");
        }

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = user.getId().equals(targetUserId);

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("No tienes permisos para acceder a este recurso.");
        }
    }

}
