package com.DigitalMoneyHouse.accountsservice.dto.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
public class TransferRequestEntryDTO {
    private String recipient; // CVU o alias
    private BigDecimal amount; // Monto a transferir
}
