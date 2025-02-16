package com.DigitalMoneyHouse.accountsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DepositMoneyRequest {
    private String cardNumber;
    private Double amount;
}
