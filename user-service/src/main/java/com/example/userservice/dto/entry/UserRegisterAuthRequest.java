package com.example.userservice.dto.entry;

import lombok.Data;

@Data
public class UserRegisterAuthRequest {
    private String email;
    private String password;
}
