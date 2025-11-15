package com.example.userservice.service.impl;

import com.example.userservice.aliasGenerator.AliasGenerator;
import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.entry.*;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.exceptions.DniAlreadyExistsException;
import com.example.userservice.exceptions.EmailAlreadyRegisteredException;
import com.example.userservice.exceptions.UnauthorizedException;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.generatorCVU.GeneratorCVU;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.IUserService;
import com.example.userservice.service.client.AccountClient;
import com.example.userservice.service.client.AuthClient;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AccountClient accountClient;
    private final AuthClient authClient;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper,
                           AccountClient accountClient, AuthClient authClient) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.accountClient = accountClient;
        this.authClient = authClient;
    }

    // ✅ REGISTRO COMPLETO
    @Override
    @Transactional
    public UserRegisterOutDto handleRegister(UserRegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException("El email ya está registrado");
        }
        if (userRepository.existsByDni(request.getDni())){
           throw new DniAlreadyExistsException("El DNI ingresado ya esta registrado");
        }
        // 🔐 Registrar en AuthService
        UserRegisterAuthRequest authRequest = new UserRegisterAuthRequest();
        authRequest.setEmail(request.getEmail());
        authRequest.setPassword(request.getPassword());

        AuthResponse authResponse = authClient.registerUser(authRequest);

        // 🧍 Crear usuario en UserService
        User user = new User();
        user.setAuthId(authResponse.getAuthId());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setDni(request.getDni());
        user.setAlias(generateUniqueAlias());
        user.setCvu(GeneratorCVU.generateCVU());

        User saved = userRepository.save(user);

        // 💳 Crear cuenta en AccountService
        AccountCreationRequest accountReq = new AccountCreationRequest();
        accountReq.setUserId(saved.getId());
        accountReq.setEmail(saved.getEmail());
        accountReq.setAlias(saved.getAlias());
        accountReq.setCvu(saved.getCvu());
        accountReq.setInitialBalance(BigDecimal.ZERO);

        var accountResp = accountClient.createAccount(accountReq);
        saved.setAccountId(accountResp.getId());
        userRepository.save(saved);

        return UserRegisterOutDto.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .dni(saved.getDni())
                .phone(saved.getPhone())
                .authId(saved.getAuthId())
                .accountId(accountResp.getId())
                .email(saved.getEmail())
                .cvu(saved.getCvu())
                .alias(saved.getAlias())
                .balance(accountResp.getBalance())
                .token(authResponse.getToken())
                .build();
    }

    // ✅ GET USER — compara email del token
    @Override
    public User getUserById(Long id, String email) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));


        return user;
    }

    // ✅ DELETE USER — dueño o admin (validación la hace controller)
    @Transactional
    @Override
    public void deleteUser(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        try {
            log.info("emtrando al account");
            if (user.getAccountId() != null) {
                accountClient.deleteAccount(user.getAccountId());
            }

            log.info("dentro de auth");
            if (user.getAuthId() != null){
                String response = authClient.deleteUserAuth(user.getAuthId());
                log.info("Respuesta del auth-service al eliminar auth: {}", response);
            }

            log.info("🔥 Eliminando usuario LOCAL en UserService...");
            userRepository.delete(user);   // ← AHORA SÍ SE EJECUTA

        } catch (Exception e) {
            log.error("❌ Error eliminando usuario: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar usuario");
        }
    }

    @Override
    public void updateUser(Long userId, UserEntryDto dto, String email) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));


        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getDni() != null) user.setDni(dto.getDni());
// ✅ Si se quiere actualizar email o role
        boolean updateEmail = dto.getEmail() != null && !dto.getEmail().equals(user.getEmail());
        boolean updateRole  = dto.getRole() != null;
        if (updateEmail && userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyRegisteredException("El email nuevo ya está registrado");
        }

        if (updateEmail || updateRole) {

            UserUpdateRequest authRequest = new UserUpdateRequest();

            if (updateEmail) {
                authRequest.setEmail(dto.getEmail());
            }

            if (updateRole) {
                authRequest.setRole(dto.getRole());
            }
            authRequest.setId(user.getAuthId());
            // ✅ Actualizar en AuthService primero
            log.info(authRequest.getRole() + " update user " + authRequest.getUserId() + " " + authRequest.getEmail());
            try {
                 authClient.updateUserAuth(authRequest);
                log.info("✅ Llamada a AuthService enviada correctamente");
            } catch (Exception e) {
                log.error("❌ Error Feign al llamar a AuthService: {}", e.getMessage(), e);
            }
            // ✅ Solo aplicar cambios locales si AuthService dijo OK
            if (updateEmail) {
                user.setEmail(dto.getEmail());
            }
            if (updateRole) {
                user.setRole(dto.getRole());
            }
        }

        userRepository.save(user);
    }


    @Override
    public void updateAlias(Long id, String alias, String email) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));


        if (alias == null || alias.trim().isEmpty()) {
            throw new BadRequestException("Alias vacío");
        }

        user.setAlias(alias);
        userRepository.save(user);
    }

    @Override
    public Map<String, Object> handleUserRegistration(UserEntryDto userEntryDto) throws IOException {
        throw new UnsupportedOperationException("No implementado — usar handleRegister()");
    }

    private String generateUniqueAlias() {
        String alias;
        do {
            alias = AliasGenerator.generateAlias();
        } while (userRepository.existsByAlias(alias));

        return alias;
    }

    @Override
    public User findByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return user;
    }
}
