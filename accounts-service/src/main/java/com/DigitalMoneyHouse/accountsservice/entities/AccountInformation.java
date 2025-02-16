package com.DigitalMoneyHouse.accountsservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountInformation {
    private Long id;
    private Long userId;
    private Double balance;
    private String cvu;
    private String alias;
}
