package com.example.userservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerificationCode {
    private String email;
    private String verificationCode;
}
