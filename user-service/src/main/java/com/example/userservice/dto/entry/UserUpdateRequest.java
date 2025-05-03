package com.example.userservice.dto.entry;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String password;
}
