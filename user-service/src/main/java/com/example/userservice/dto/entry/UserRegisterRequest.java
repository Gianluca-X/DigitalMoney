package com.example.userservice.dto.entry;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String dni;
}

