package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Data
public class RefreshToken {

    @Id
    @GeneratedValue
    private Long id;

    private String token;

    private Long userId;

    private LocalDateTime expiryDate;
}