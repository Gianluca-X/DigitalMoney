package com.example.userservice.service.impl;

import com.example.userservice.aliasGenerator.AliasGenerator;
import com.example.userservice.dto.entry.*;
import com.example.userservice.exceptions.*;
import com.example.userservice.generatorCVU.GeneratorCVU;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.IUserService;
import com.example.userservice.service.client.AccountClient;
import jakarta.ws.rs.BadRequestException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

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

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, AccountClient accountClient) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.accountClient = accountClient;
    }
    @Override
    @Transactional
    public void createUserFromEvent(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("El usuario con email {} ya existe. No se creará de nuevo.", request.getEmail());
            return;
        }

        String alias = generateUniqueAlias();
        String cvu = GeneratorCVU.generateCVU();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setDni(request.getDni());
        user.setAlias(alias);
        user.setCvu(cvu);
        user.setAuthId(request.getAuthId);

        User savedUser = userRepository.save(user);

        // Crear cuenta en Account Service
        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setUserId(savedUser.getId());
        accountRequest.setEmail(savedUser.getEmail());
        accountRequest.setAlias(savedUser.getAlias());
        accountRequest.setCvu(savedUser.getCvu());
        accountRequest.setInitialBalance(BigDecimal.ZERO);

        AccountResponse accountResponse = accountClient.createAccount(accountRequest, "Bearer " + getAdminToken());
        savedUser.setAccountId(accountResponse.getId());
        userRepository.save(savedUser);

        log.info("👤 Usuario creado desde evento con email: {}", request.getEmail());
    }

    @Override
    public UserRegisterOutDto getUserById(Long id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            // Si el JWT está presente, usas el correo del JWT para obtener el usuario
            String email = jwt.getClaim("email");
            logger.info("📥 Buscando usuario autenticado con email: " + email);

            User userBuscado = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

            return modelMapper.map(userBuscado, UserRegisterOutDto.class);
        } else {
            // Si el JWT no está presente, obtienes el usuario por ID
            User userBuscado = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

            return modelMapper.map(userBuscado, UserRegisterOutDto.class);
        }
    }

    private void registerUserInKeycloak(User user, String password) {
        String keycloakUrl = "http://localhost:8080/admin/realms/DigitalMoneyHouse/users";
        String adminToken = getAdminToken();

        Map<String, Object> keycloakUser = new HashMap<>();
        keycloakUser.put("username", user.getEmail());
        keycloakUser.put("email", user.getEmail());
        keycloakUser.put("enabled", true);
        keycloakUser.put("credentials", List.of(Map.of("type", "password", "value", password, "temporary", false)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(keycloakUser, headers);
        new RestTemplate().postForEntity(keycloakUrl, request, String.class);
    }
    private String getAdminToken() {
        String keycloakUrl = "http://localhost:8080/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=admin-cli&username=admin&password=admin&grant_type=password";

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(keycloakUrl, request, Map.class);
        return response.getBody().get("access_token").toString();
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

    private String generateUniqueAlias() {
        String alias;
        do {
            alias = AliasGenerator.generateAlias();
        } while (userRepository.existsByAlias(alias));
        return alias;
    }
}
