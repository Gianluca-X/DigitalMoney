package com.example.userservice.dto.entry;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private Long id;
    private String email;
    private String alias;
    private String cvu;
    private BigDecimal balance;
}
