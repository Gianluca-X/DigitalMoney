package com.DigitalMoneyHouse.accountsservice.dto.exit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
public class RecipientOutDTO {
    private String recipient; // CBU/CVU/alias
    private BigDecimal lastAmount; // Último monto transferido


}
