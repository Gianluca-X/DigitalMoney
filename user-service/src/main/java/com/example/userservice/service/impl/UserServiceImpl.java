package com.example.userservice.service.impl;

import com.example.userservice.aliasGenerator.AliasGenerator;
import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.entry.*;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.exceptions.*;
import com.example.userservice.generatorCVU.GeneratorCVU;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.IUserService;
import com.example.userservice.service.client.AccountClient;
import com.example.userservice.service.client.AuthClient;
import jakarta.ws.rs.BadRequestException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AccountClient accountClient;
    private final AuthClient authClient;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, AccountClient accountClient, AuthClient authClient) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.accountClient = accountClient;
        this.authClient = authClient;
    }
    @Override
    @Transactional
    public UserRegisterOutDto handleRegister(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("El usuario con email {} ya existe. No se creará de nuevo.", request.getEmail());
            throw new EmailAlreadyRegisteredException("El email " + request.getEmail() + " ya está registrado");
        }


        // 1️⃣ Registrar en AuthService para obtener authId
        UserRegisterAuthRequest authRequest = new UserRegisterAuthRequest();
        authRequest.setEmail(request.getEmail());
        authRequest.setPassword(request.getPassword()); // Se enviará solo al auth service

        AuthResponse authResponse = authClient.registerUser(authRequest);

        // 2️⃣ Crear usuario en UserService
        String alias = generateUniqueAlias();
        String cvu = GeneratorCVU.generateCVU();

        User user = new User();
        user.setAuthId(authResponse.getAuthId());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setDni(request.getDni());
        user.setAlias(alias);
        user.setCvu(cvu);

        User savedUser = userRepository.save(user);

        // 3️⃣ Crear cuenta en Account Service
        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setUserId(savedUser.getId());
        accountRequest.setEmail(savedUser.getEmail());
        accountRequest.setAlias(savedUser.getAlias());
        accountRequest.setCvu(savedUser.getCvu());
        accountRequest.setInitialBalance(BigDecimal.ZERO);

        AccountResponse accountResponse = accountClient.createAccount(accountRequest);
        savedUser.setAccountId(accountResponse.getId());
        userRepository.save(savedUser);

        log.info("👤 Usuario creado con authId: {}", authResponse.getAuthId());
        log.info("Cuenta Creada con account id: " + accountResponse.getId());
        // Luego, devolver algo como:
        return UserRegisterOutDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dni(user.getDni())
                .phone(user.getPhone())
                .authId(user.getAuthId())
                .accountId(accountResponse.getId())
                .email(user.getEmail())
                .cvu(user.getCvu())
                .alias(user.getAlias())
                .balance(accountResponse.getBalance())
                .token(authResponse.getToken())
                .build();

    }

    @Override
    public User getUserById(Long id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            // Si el JWT está presente, usas el correo del JWT para obtener el usuario
            String email = jwt.getClaim("email");
            logger.info("📥 Buscando usuario autenticado con email: " + email);

            User userBuscado = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado by mail"));

            return modelMapper.map(userBuscado, User.class);
        } else {
            // Si el JWT no está presente, obtienes el usuario por ID
            User userBuscado = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado p0r id"));

            return modelMapper.map(userBuscado, User.class);
        }
    }


    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public void updateUser(Long userId, @NonNull UserEntryDto userEntryDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        modelMapper.map(userEntryDto, existingUser);
        userRepository.save(existingUser);
    }
    public void updateUserEmail(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(newEmail);
        userRepository.save(user);
    }





    public void updateAlias(Long id, String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            throw new BadRequestException("El alias no puede estar vacío");
        }

        int updatedRows = userRepository.updateAlias(id, alias);
        if (updatedRows == 0) {
            throw new UserNotFoundException("Usuario no encontrado");
        }
    }
    @Override
    public Map<String, Object> handleUserRegistration(UserEntryDto userEntryDto) throws IOException {
        return null;
    }

    private String generateUniqueAlias() {
        String alias;
        do {
            alias = AliasGenerator.generateAlias();
        } while (userRepository.existsByAlias(alias));
        return alias;
    }
}
