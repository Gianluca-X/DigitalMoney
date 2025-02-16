package com.DigitalMoneyHouse.accountsservice.dto.exit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCardOutDTO {
    private String number;
    private String name;
    private String expiry;
    private String cvc;


}
