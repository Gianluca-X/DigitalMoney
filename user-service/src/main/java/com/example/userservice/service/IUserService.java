package com.example.userservice.service;

import com.example.userservice.dto.entry.LoginRequest;
import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import com.example.userservice.exceptions.EmailNotVerifiedException;
import com.example.userservice.exceptions.IncorrectPasswordException;
import com.example.userservice.exceptions.UserNotFoundException;
import lombok.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;


import java.io.IOException;

import java.util.Map;

public interface IUserService {

    User createUser(@NonNull UserEntryDto userEntryDto);
    void deleteUser(Long userId);
    void updateUser(Long userId, UserEntryDto userEntryDto);

    void logoutUser(String token);
    String authenticateAndLogin(LoginRequest request) throws UserNotFoundException, IncorrectPasswordException, EmailNotVerifiedException;
    void verifyUserEmail(String email, String verificationCode);

    UserRegisterOutDto getUserById(Long id, Jwt jwt);
    Map<String, Object> handleUserRegistration(UserEntryDto userEntryDto) throws IOException;
    void processPasswordResetRequest(String email);
    void resetPassword(String token, String newPassword, String confirmPassword);
    //void updateAlias(Long id, String alias);
}
