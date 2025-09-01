package com.example.userservice.service;

import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.entry.UserRegisterRequest;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;


import java.io.IOException;

import java.util.Map;

public interface IUserService {

    User getUserById(Long id, @AuthenticationPrincipal Jwt jwt);

    void deleteUser(Long userId);
    void updateUser(Long userId, UserEntryDto userEntryDto);

    void updateAlias(Long id, String alias );
    Map<String, Object> handleUserRegistration(UserEntryDto userEntryDto) throws IOException;
    void updateUserEmail(Long id, String email);

    UserRegisterOutDto handleRegister(UserRegisterRequest userEntryDto);
}
