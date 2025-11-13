package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
import com.mysql.cj.log.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private EmailService emailService;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private UserEventPublisher userEventPublisher;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar SecurityContext con Authentication mockeado
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    @Test
    void register_ShouldRegisterUserAndSendEmailAndPublishEvent() {
        // Arrange
        // Solo email y password, que es lo que realmente usa AuthService
        UserEntry entry = new UserEntry("nerea@example.com", "clave123");

        // Simulamos el usuario que se guarda en la DB
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(entry.getEmail());
        savedUser.setPassword("hashed");
        savedUser.setVerificationCode("code123");
        savedUser.setEmailVerified(false);

        // Mock del passwordEncoder
        when(passwordEncoder.encode(entry.getPassword())).thenReturn("hashed");

        // Cuando guardamos en la repo, devolvemos el usuario con ID
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Mock del JWT
        when(jwtUtil.generateToken(entry)).thenReturn("mocked-token");

        // Act


        AuthResponse response = authService.register(entry);
        response.setAuthId(1L);
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getAuthId());           // Ahora authId viene del savedUser
        assertEquals("mocked-token", response.getToken());

        // Verificamos que se haya enviado el email
        verify(emailService).sendVerificationEmail(eq(entry.getEmail()), savedUser.getVerificationCode(), any(String.class));

        // Si publicás eventos, verificamos que se llame al publicador
    }


    @Test
    void login_ShouldReturnTokenIfCredentialsAreValid() {
        User userFromDb = new User();
        userFromDb.setEmail("user@mail.com");
        userFromDb.setPassword("hashed");
        userFromDb.setEmailVerified(true);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(userFromDb));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken(userFromDb)).thenReturn("token123");

        LoginRequest loginUser = new LoginRequest();
        loginUser.setEmail("user@mail.com");
        loginUser.setPassword("123456");

        AuthResponse authResponse = authService.login(loginUser);
        assertEquals("token123", token);
    }

    @Test
    void login_ShouldThrowIfEmailNotVerified() {
        User userFromDb = new User();
        userFromDb.setEmail("user@mail.com");
        userFromDb.setPassword("hashed");
        userFromDb.setEmailVerified(false);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(userFromDb));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);

        LoginRequest loginUser = new LoginRequest();
        loginUser.setEmail("user@mail.com");
        loginUser.setPassword("123456");

        assertThrows(RuntimeException.class, () -> authService.login(loginUser));
    }

    @Test
    void changeEmail_ShouldUpdateEmailAndPublishEvent() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

        authService.changeEmail("new@mail.com");

        assertEquals("new@mail.com", user.getEmail());
        verify(userRepository).save(user);
        verify(rabbitTemplate).convertAndSend(eq("user.exchange"), eq("user.email.changed"), any(UserEmailChangedEvent.class));
    }

    @Test
    void changePassword_ShouldUpdatePassword() {
        User user = new User();
        user.setPassword("old");

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");

        authService.changePassword("newPass");

        assertEquals("newEncoded", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_ShouldSetEmailVerifiedToTrue() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setEmailVerified(false);
        user.setVerificationCode("code123");

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

        authService.verifyEmail("code123");

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
    }
}
