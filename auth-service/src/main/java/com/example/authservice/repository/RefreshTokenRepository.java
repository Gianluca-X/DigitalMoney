package com.example.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.example.authservice.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}