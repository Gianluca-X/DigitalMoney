package com.example.userservice.dto.entry;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AccountCreationRequest {
    private Long userId;
    private String email;
    private String alias;
    private String cvu;
    private BigDecimal initialBalance;
}
