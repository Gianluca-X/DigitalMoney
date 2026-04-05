package com.example.authservice.service;
import com.example.authservice.entity.RefreshToken;
import com.example.authservice.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    // ✅ CAMBIAR NOMBRE
    public RefreshToken create(Long userId) {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        return repo.save(token);
    }

    public RefreshToken validate(String tokenStr) {
        RefreshToken token = repo.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expirado");
        }

        return token;
    }

    // ✅ AGREGAR ESTE MÉTODO (te falta)
    public void delete(RefreshToken token) {
        repo.delete(token);
    }
}