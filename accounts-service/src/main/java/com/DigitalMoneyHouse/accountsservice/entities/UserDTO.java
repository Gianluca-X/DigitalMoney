package com.DigitalMoneyHouse.accountsservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private String name;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private String cvu;
    private String alias;
}
