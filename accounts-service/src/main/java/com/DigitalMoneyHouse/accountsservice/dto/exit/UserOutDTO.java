package com.DigitalMoneyHouse.accountsservice.dto.exit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserOutDTO {
    private Long id;
    private String email;
    private String alias;
    private String cvu;
    private String username;
    private String password;
}
