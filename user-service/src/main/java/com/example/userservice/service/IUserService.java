package com.example.userservice.service;

import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.entry.UserRegisterRequest;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.exceptions.DniAlreadyExistsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.util.Map;

public interface IUserService {

    User getUserById(Long id, String email);
    void deleteUser(Long userId, String email);
    void updateUser(Long userId, UserEntryDto userEntryDto, String email);
    void updateAlias(Long id, String alias, String email);

    Map<String, Object> handleUserRegistration(UserEntryDto userEntryDto) throws IOException;

    UserRegisterOutDto handleRegister(UserRegisterRequest request) throws DniAlreadyExistsException;

    User findByEmail(String email);
}
