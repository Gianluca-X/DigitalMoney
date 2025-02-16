package com.DigitalMoneyHouse.accountsservice.dto.exit;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityOutDTO {
    private Long id;
    private Long accountId;
    private String type; // Tipo de actividad (ejemplo: "pago", "carga", etc.)
    private BigDecimal amount;
    private String date; // Fecha de la actividad
    private String cvu;


}
