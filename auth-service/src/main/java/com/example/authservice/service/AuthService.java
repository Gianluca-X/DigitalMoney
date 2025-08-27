package com.example.authservice.service;
import java.util.UUID;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.UserEntry;
import com.example.authservice.dto.UserRegisterRequest;
import com.example.authservice.dto.UserEmailChangedEvent;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
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
    public String register(UserEntry userEntry) {
        // Crear DTO para el registro en user-service
        UserRegisterRequest request = new UserRegisterRequest();
        request.setPhone(userEntry.getPhone());
        request.setEmail(userEntry.getEmail());
        request.setFirstName(userEntry.getFirstName());
        request.setLastName(userEntry.getLastName());
        request.setDni(userEntry.getDni());


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
        request.setAuthId(user.getId());
        userEventPublisher.publishRegisterEvent(request);

        // Generar el token JWT
        return jwtUtil.generateToken(user.getEmail());
    }
    // Login del usuario
    public String login(LoginRequest user) {
        return userRepository.findByEmail(user.getEmail())
                .filter(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()))
//                .filter(User::isEmailVerified) // Asegura que el email esté verificado
                .map(u -> jwtUtil.generateToken(u.getEmail()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials or email not verified"));
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
