package com.DigitalMoneyHouse.accountsservice.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Data
@Getter
@Setter
public class AccountResponse {
    private Long Id;
    private BigDecimal balance;


}
