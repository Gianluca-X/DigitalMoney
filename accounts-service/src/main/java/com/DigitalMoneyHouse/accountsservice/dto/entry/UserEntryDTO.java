package com.DigitalMoneyHouse.accountsservice.dto.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserEntryDTO {
    private String email;
    private String alias;
    private String cvu;
    private String username;
    private String password;
}
