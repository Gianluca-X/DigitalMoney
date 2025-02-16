package com.DigitalMoneyHouse.accountsservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Transference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;
    private Long cardId;
    private BigDecimal amount;
    private LocalDateTime date;
    private String type;  // "CREDIT" o "DEBIT"
    private String recipient;  // identificador del destinatario


}

