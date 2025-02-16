package com.example.userservice.dto.exit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterOutDto {

    private Long id;
    //private String userName;
    private String firstName;

    private String lastName;

    private int dni;

    private int phone;

    private String email;

    private String cvu;

    private String alias;
    private String token;

}
