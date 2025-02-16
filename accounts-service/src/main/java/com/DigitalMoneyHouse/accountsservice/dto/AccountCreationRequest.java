package com.DigitalMoneyHouse.accountsservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor // Agrega el constructor con todos los argumentos
@NoArgsConstructor
public class AccountCreationRequest {
    private Long userId;
    private String email;
    private String alias;
    private String cvu;
    private BigDecimal initialBalance;



}
