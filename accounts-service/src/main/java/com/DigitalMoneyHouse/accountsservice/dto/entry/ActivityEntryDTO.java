package com.DigitalMoneyHouse.accountsservice.dto.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
public class ActivityEntryDTO {
    private Long accountId;
    private String type; // Tipo de actividad (ejemplo: "pago", "carga", etc.)
    private BigDecimal amount;
    private String date; // Fecha de la actividad
    private String cvu;

}
