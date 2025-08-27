    package com.example.userservice.controller;
    
    import com.example.userservice.dto.entry.UserEntryDto;
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
            return ResponseEntity.noContent().build();
        }
    
        @PatchMapping("/update/alias/{id}")
        public ResponseEntity<?> updateAlias(@PathVariable Long id, @RequestBody UserAliasUpdateRequest request) {
            iuserService.updateAlias(id, request.getAlias());
            return ResponseEntity.ok("Alias actualizado");
        }
    }
