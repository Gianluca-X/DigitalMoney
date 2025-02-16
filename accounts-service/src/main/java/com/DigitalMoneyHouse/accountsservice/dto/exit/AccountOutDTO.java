package com.DigitalMoneyHouse.accountsservice.dto.exit;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountOutDTO {
    private Long id;          // ID de la cuenta
    private Long userId;// ID del usuario al que pertenece la cuenta
    private String alias;
    private String cvu;
    private BigDecimal balance; // Saldo de la cuenta
    private List<String> transactions;


}
