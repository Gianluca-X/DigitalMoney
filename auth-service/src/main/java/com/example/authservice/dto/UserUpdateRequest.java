package com.example.authservice.dto;

import com.example.authservice.entity.Role;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Data
public class UserUpdateRequest {
    private Long id;

    private String email;
    Role role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

