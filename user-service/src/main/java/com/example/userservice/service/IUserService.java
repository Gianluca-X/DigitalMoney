package com.example.userservice.service;

import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.entry.UserRegisterRequest;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import lombok.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;


import java.io.IOException;

import java.util.Map;

public interface IUserService {

    User createUser(@NonNull UserEntryDto userEntryDto);
    void deleteUser(Long userId);
    void updateUser(Long userId, UserEntryDto userEntryDto);
    void createUserFromEvent(UserRegisterRequest request);


    UserRegisterOutDto getUserById(Long id, Jwt jwt);
    Map<String, Object> handleUserRegistration(UserEntryDto userEntryDto) throws IOException;
    void updateUserEmail(Long id, String email);
}
