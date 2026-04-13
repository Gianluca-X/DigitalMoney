    package com.example.authservice.service;
    import java.util.Optional;
    import java.util.UUID;
    import org.springframework.beans.factory.annotation.Value;
    import com.example.authservice.dto.*;
    import com.example.authservice.entity.Role;
    import com.example.authservice.entity.User;
    import com.example.authservice.exceptions.EmailNotVerifiedException;
    import com.example.authservice.exceptions.InvalidPasswordException;
    import com.example.authservice.exceptions.InvalidVerificationCodeException;
    import com.example.authservice.exceptions.UserNotFoundException;
    import com.example.authservice.repository.UserRepository;
    import com.example.authservice.security.JwtUtil;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.amqp.rabbit.core.RabbitTemplate;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import com.example.authservice.entity.RefreshToken;
    import java.util.Map;

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
        private final RefreshTokenService refreshTokenService;
        @Value("${frontend.url}")
        private String frontUrl;

        // Registro de un usuario
        public AuthResponse register(UserEntry userEntry) {

            // Guardar en auth_db
            User user = new User();
            user.setEmail(userEntry.getEmail());
            user.setPassword(passwordEncoder.encode(userEntry.getPassword()));
            user.setRol(
                    userEntry.getRol() == null ? Role.USER : userEntry.getRol()
            );

            String verificationCode = UUID.randomUUID().toString();
            user.setVerificationCode(verificationCode);
            user.setEmailVerified(false);
            userRepository.save(user);

            // Enviar el email
            String verificationLink = frontUrl + "/verify-email?code=" + verificationCode;
            emailService.sendVerificationEmail(user.getEmail(), verificationCode, verificationLink);
            // Publicar el evento de registro de usuario

            // Generar el token JWT
            String token = jwtUtil.generateToken(user);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAuthId(user.getId());
            authResponse.setToken(null);
            return authResponse;
        }
        public AuthResponse login(LoginRequest request) {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("Usuario inexistente"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new InvalidPasswordException("Contraseña incorrecta");
            }

            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Email no verificado"); // Opcional, 400 o 403 según política
            }
            String accessToken = jwtUtil.generateToken(user);

            RefreshToken refreshToken = refreshTokenService.create(user.getId());
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAuthId(user.getId());
            authResponse.setToken(accessToken);
            authResponse.setRefreshToken(refreshToken.getToken()); 
            authResponse.setMessage("Login Exitoso");
            return authResponse;

        }

        // Cambio de email
        public void changeEmail(String newEmail) {
            log.info("nuevo email: " + newEmail);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.error("❌ Authentication es NULL en SecurityContextHolder");
                throw new RuntimeException("No hay usuario autenticado");
            }

            String currentUserEmail = auth.getName();
            log.info("🔑 Usuario autenticado en token: {}", currentUserEmail);            log.info("current email user: " + currentUserEmail);
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            user.setEmail(newEmail);
            userRepository.save(user);

            UserEmailChangedEvent event = new UserEmailChangedEvent(user.getId(), newEmail);
            rabbitTemplate.convertAndSend("user.exchange", "user.email.changed", event);

            log.info("Email changed for user: {}. Event sent to user-service.", user.getId());
        }

        public void changePassword(String newPassword) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.error("❌ Authentication es NULL en SecurityContextHolder");
                throw new RuntimeException("No hay usuario autenticado");
            }

            String email = auth.getName();
            log.info("🔑 Usuario autenticado en token: {}", email);
            log.info("email del user: " + email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            log.info("Password changed for user: {}" + email);
        }
        public void verifyEmail(String code) {
            String cleanCode = code.trim().replace("\"", "");
            log.info("Código recibido: {}", cleanCode);

            User user = userRepository.findByVerificationCode(cleanCode)
                    .orElseThrow(() -> new InvalidVerificationCodeException("Código inválido o expirado"));
            log.info("Código en DB: {}", user.getVerificationCode());

            user.setEmailVerified(true);
            user.setVerificationCode(null); 
            userRepository.save(user);

            log.info("✅ Email verified for user: {}", user.getEmail());
        }
        public AuthResponse updateUser(UserUpdateRequest request) {
        log.info("Accediendo a update user." + request.getId());
            User user = userRepository.findById(request.getId())
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
            log.info(user.getId() + "id user");
            if (request.getEmail() != null)
                user.setEmail(request.getEmail());

            if (request.getRole() != null)
                user.setRol(request.getRole());

            userRepository.save(user);

            String token = jwtUtil.generateToken(user);
        AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setMessage("Usuario actualizado con exito");
            authResponse.setAuthId(user.getId());
            return authResponse;
        }

        public String deleteUser(Long id) {

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            userRepository.delete(user);
            return "Usuario de auth eliminado con éxito";
        }
    public void resendVerification(String email) {

    User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

    if (user.isEmailVerified()) {
    throw new RuntimeException("El email ya está verificado");
    }

    String verificationCode = UUID.randomUUID().toString();

        user.setVerificationCode(verificationCode);
            userRepository.save(user);

    String verificationLink =
    frontUrl + "/verify-email?code=" + verificationCode;

    emailService.sendVerificationEmail(
    user.getEmail(),
                verificationCode,
                            verificationLink
                                );

                                    log.info("Nuevo código de verificación enviado a {}", email);
                                    }
      
                                    
        public Map<String, String> refresh(String refreshTokenStr) {

            RefreshToken oldToken = refreshTokenService.validate(refreshTokenStr);

            // ❌ eliminar viejo
            refreshTokenService.delete(oldToken);

            // ✅ crear nuevo
            RefreshToken newToken = refreshTokenService.create(oldToken.getUserId());

            User user = userRepository.findById(oldToken.getUserId())
                    .orElseThrow();

            String newAccessToken = jwtUtil.generateToken(user);

            return Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newToken.getToken()
            );
        }                                
    }

