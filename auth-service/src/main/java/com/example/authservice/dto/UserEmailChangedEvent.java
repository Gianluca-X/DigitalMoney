package com.example.authservice.dto;
public class UserEmailChangedEvent {
    private Long userId;
    private String newEmail;

    // Constructor, getters, and setters
    public UserEmailChangedEvent(Long userId, String newEmail) {
        this.userId = userId;
        this.newEmail = newEmail;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}

