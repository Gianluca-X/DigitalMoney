package com.DigitalMoneyHouse.accountsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CreateTransaction {
    private int senderId;
    private int receiverId;
    private Double amountOfMoney;
    private LocalDateTime date;
}
