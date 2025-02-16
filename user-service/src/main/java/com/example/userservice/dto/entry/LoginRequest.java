package com.example.userservice.dto.entry;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {
    private String email;
    private String password;
}
