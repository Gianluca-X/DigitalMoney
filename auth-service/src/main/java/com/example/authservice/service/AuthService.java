package com.example.authservice.service;
import java.util.UUID;

import com.example.authservice.dto.*;
import com.example.authservice.entity.User;
import com.example.authservice.exceptions.EmailNotVerifiedException;
import com.example.authservice.exceptions.InvalidPasswordException;
import com.example.authservice.exceptions.UserNotFoundException;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
import com.mysql.cj.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate; // Para enviar eventos a RabbitMQ
    private final UserEventPublisher userEventPublisher; // Inyectar el publicador de eventos

    // Registro de un usuario
    public AuthResponse register(UserEntry userEntry) {

        // Guardar en auth_db
        User user = new User();
        user.setEmail(userEntry.getEmail());
        user.setPassword(passwordEncoder.encode(userEntry.getPassword()));
        userRepository.save(user);
        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);
        user.setEmailVerified(false);
        userRepository.save(user);

        // Enviar el email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        // Publicar el evento de registro de usuario

        // Generar el token JWT
        String token = jwtUtil.generateToken(user.getEmail());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAuthId(user.getId());
        authResponse.setToken(token);
        return authResponse;
    }
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario inexistente"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Contraseña incorrecta");
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email no verificado"); // Opcional, 400 o 403 según política
        }

       return jwtUtil.generateToken(user.getEmail());
    }

    // Cambio de email
    public void changeEmail(String newEmail) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(newEmail);
        userRepository.save(user);

        UserEmailChangedEvent event = new UserEmailChangedEvent(user.getId(), newEmail);
        rabbitTemplate.convertAndSend("user.exchange", "user.email.changed", event);

        log.info("Email changed for user: {}. Event sent to user-service.", user.getId());
    }

    // Cambio de contraseña
    public void changePassword(String newPassword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Actualizar la contraseña en auth_db
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}" + email);
    }
    public void verifyEmail(String code) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("codigo antes de el if " + user.getVerificationCode());
        if(user.getVerificationCode().equals(code)) {
            user.setEmailVerified(true);
            log.info("codigo: " + user.getVerificationCode());
            userRepository.save(user);
            log.info("Email verified for user: {}", email);
        }
    }

}
