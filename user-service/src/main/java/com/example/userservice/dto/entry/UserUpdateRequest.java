package com.example.userservice.dto.entry;

import com.example.userservice.entity.Role;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long id;
    private String email;
    Role role;

    public Long getUserId() {
        return id;
    }
    public String getNewEmail(){
        return email;
    }
}
