package com.DigitalMoneyHouse.accountsservice.dto.exit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferenceOutDTO {
    private Long cardId;
    private BigDecimal amount;

}

