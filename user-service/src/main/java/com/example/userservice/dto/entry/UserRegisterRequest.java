package com.example.userservice.dto.entry;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String dni;
    private Long authId;
}
