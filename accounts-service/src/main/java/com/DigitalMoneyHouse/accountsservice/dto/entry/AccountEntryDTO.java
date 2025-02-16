package com.DigitalMoneyHouse.accountsservice.dto.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
public class AccountEntryDTO {
    private Long userId;// ID del usuario al que pertenece la cuenta
    private String alias;
    private String cvu;
    private BigDecimal balance; // Saldo de la cuenta
    private List<String> transactions;

}
