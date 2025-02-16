package com.example.userservice.service.impl;

import com.example.userservice.aliasGenerator.AliasGenerator;



import com.example.userservice.config.jwt.TokenManager;
import com.example.userservice.dto.entry.*;


import com.example.userservice.exceptions.*;
import com.example.userservice.generatorCVU.GeneratorCVU;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.VerificationTokenRepository;
import com.example.userservice.service.IUserService;
import com.example.userservice.service.client.AccountClient;
import com.example.userservice.utils.JsonPrinter;
import jakarta.ws.rs.BadRequestException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {
    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    private AccountClient accountClient;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private TokenManager tokenManager;
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder,TokenBlacklistService tokenBlacklistService, EmailService emailService, AccountClient accountClient, TokenManager tokenManager ) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.accountClient = accountClient;
        this.emailService = emailService;
        this.tokenManager=tokenManager;

    }
    public Map<String, Object> handleUserRegistration(UserEntryDto userEntryDto) throws IOException {
        User registeredUser = createUser(userEntryDto);
        String verificationCode = generateVerificationCode(registeredUser);
        emailService.sendVerificationEmail(registeredUser.getEmail(), verificationCode);
        return buildUserResponse(registeredUser);
    }

    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("dni", user.getDni());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("cvu", user.getCvu());
        response.put("alias", user.getAlias());
        return response;
    }
    /**
     * Método para crear un usuario en la base de datos.
     */
    @Transactional
    public User createUser(@NonNull UserEntryDto userEntryDto) {
        // Verificar si el usuario o correo ya existen en la base de datos
        if (userRepository.existsByEmail(userEntryDto.getEmail())) {
            log.error("User with email {} already exists.", userEntryDto.getEmail());
            throw new EmailAlreadyRegisteredException("Email is already registered!");
        }
        log.info("UserEntryDto: {}", userEntryDto);
        // Generar alias y CVU
        String alias = generateUniqueAlias();
        String cvu = GeneratorCVU.generateCVU();

        // Mapear y guardar el usuario en la base de datos
        User user = modelMapper.map(userEntryDto, User.class);
        user.setAlias(alias);
        user.setCvu(cvu);
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(userEntryDto.getPassword()));
        log.info("Encoded password for user {}: {}", userEntryDto.getEmail(), user.getPassword());
        User registeredUser = userRepository.save(user);

        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setUserId(registeredUser.getId());
        accountRequest.setEmail(registeredUser.getEmail());
        accountRequest.setAlias(registeredUser.getAlias());
        accountRequest.setCvu(registeredUser.getCvu());
        accountRequest.setInitialBalance(BigDecimal.ZERO);

        AccountResponse accountResponse = accountClient.createAccount(accountRequest);

        registeredUser.setAccountId(accountResponse.getId());
        userRepository.save(registeredUser);

        return registeredUser;
    }

    /**
     * Método para borrar un usuario en la base de datos.
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Método para actualizar un usuario en la base de datos.
     */
    public void updateUser(Long userId, @NonNull UserEntryDto userEntryDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        modelMapper.map(userEntryDto, existingUser);
        existingUser.setPassword(passwordEncoder.encode(userEntryDto.getPassword()));
        userRepository.save(existingUser);
    }

    @Override
    public UserRegisterOutDto getUserById(Long id) {


       User userBuscado = userRepository.findById(id).orElse(null);
        UserRegisterOutDto userEncontrado = null;

        LOGGER.debug("User buscado con id {}: {}", id, userBuscado);
        if(userBuscado != null){
            userEncontrado = modelMapper.map(userBuscado, UserRegisterOutDto.class);
            LOGGER.info("User encontrado: {}", JsonPrinter.toString(userEncontrado));
        } else {
            LOGGER.error("El id no se encuentra registrado en la base de datos");
        }

        return userEncontrado;
    }

    /**
     * Método para verificar el correo electrónico del usuario.
     */
    public void verifyUserEmail(String email, String verificationCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(verificationCode)) {
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
        } else {
            throw new InvalidVerificationCodeException("Invalid verification code or email.");
        }
    }

    public String generateVerificationCode(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        userRepository.save(user);
        return code;
    }
    /**
     * Método para autenticar al usuario y generar un token.
     */
    public String  authenticateAndLogin(LoginRequest request) throws UserNotFoundException, IncorrectPasswordException, EmailNotVerifiedException {
        // Busca al usuario por email (o username) y lanza excepción si no existe
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario inexistente"));

        // Verifica que el email del usuario esté confirmado
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Por favor verifica tu correo electrónico utilizando el código que te fue enviado.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Password mismatch for user: {}", request.getEmail());
            throw new IncorrectPasswordException("Contraseña incorrecta");
        }
        // Genera el token JWT después de la autenticación exitosa
        return tokenManager.createToken(user.getId(),user.getEmail());


    }

    @Override
    public void logoutUser(String token) {
        // Eliminar el prefijo "Bearer " si está presente
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Invalidar el token agregándolo a la lista de blacklistedTokens
        tokenBlacklistService.invalidateToken(token);
    }


    private String generateUniqueAlias() {
        String alias;
        do {
            alias = AliasGenerator.generateAlias();
        } while (userRepository.existsByAlias(alias));
        return alias;
    }

    //Metodos para reestablecer la contraseña de usuario en el login
    public void processPasswordResetRequest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        String token = tokenManager.createToken(user.getId(), user.getEmail());

        String link = "http://localhost:3000/reset-password?token=" + token;

        emailService.sendEmail(user.getEmail(), "Recuperación de la contraseña",
                "Haz clic en el siguiente enlace para restablecer tu contraseña: " + link);
    }
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        String email = tokenManager.verifyToken(token);
        if (email == null) {
            throw new InvalidTokenException("Token inválido");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IncorrectPasswordException("Las contraseñas no coinciden");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
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


}


