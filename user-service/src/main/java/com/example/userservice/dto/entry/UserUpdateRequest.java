package com.example.userservice.dto.entry;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long id;
    private String email;

    public Long getUserId() {
        return id;
    }
    public String getNewEmail(){
        return email;
    }
}
