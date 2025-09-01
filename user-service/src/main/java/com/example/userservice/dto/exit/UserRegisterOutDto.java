package com.example.userservice.dto.exit;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterOutDto {

    private Long id;
    //private String userName;
    private String firstName;

    private String lastName;

    private String dni;

    private String phone;
    private Long authId;
    private Long accountId;
    private String email;

    private String cvu;
    private BigDecimal balance;
    private String alias;
    private String token;

}
