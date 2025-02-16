package com.DigitalMoneyHouse.accountsservice.dto;


import lombok.Data;

import java.math.BigDecimal;
@Data
public class AccountResponse {
    private Long Id;
    private BigDecimal balance;


}
