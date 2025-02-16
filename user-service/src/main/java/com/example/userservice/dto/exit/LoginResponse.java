package com.example.userservice.dto.exit;


public class LoginResponse {
    private Long userId;
    private String email;
    private String token;

    public LoginResponse(Long userId, String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    public LoginResponse() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
