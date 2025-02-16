package com.DigitalMoneyHouse.accountsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
public class CardRequest {
    private String holder;
    private String number;
    private LocalDate expirationDate;
    private String cvv;
}
