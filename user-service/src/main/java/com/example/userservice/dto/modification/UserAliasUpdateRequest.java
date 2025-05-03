package com.example.userservice.dto.modification;

import lombok.Data;

@Data
public class UserAliasUpdateRequest {
    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
