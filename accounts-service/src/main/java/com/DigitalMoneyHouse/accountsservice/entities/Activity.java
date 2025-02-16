package com.DigitalMoneyHouse.accountsservice.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Data
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    private String type; // Tipo de actividad (pago, carga, etc.)
    private BigDecimal amount;
    private LocalDateTime date; // Fecha y hora de la actividad
    private String description;



}

