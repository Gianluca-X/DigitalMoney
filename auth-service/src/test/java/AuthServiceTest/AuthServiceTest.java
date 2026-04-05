package com.example.authservice.service;
import com.example.authservice.entity.RefreshToken;
import com.example.authservice.dto.*;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
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
    @Mock private RefreshTokenService refreshTokenService;       
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("user@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void register_ShouldRegisterUserAndSendEmail() {

        UserEntry entry = new UserEntry();
        entry.setEmail("nerea@example.com");
        entry.setPassword("clave123");
        entry.setRol(Role.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("nerea@example.com");
        savedUser.setPassword("hashed");
        savedUser.setVerificationCode("code123");
        savedUser.setEmailVerified(false);

        when(passwordEncoder.encode("clave123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(entry);

        assertNotNull(response);

        verify(userRepository, times(1)).save(any(User.class));

        verify(emailService).sendVerificationEmail(
                eq("nerea@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void login_ShouldReturnTokenIfCredentialsAreValid() {

        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");
        user.setPassword("hashed");
        user.setEmailVerified(true);

        when(userRepository.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "hashed"))
                .thenReturn(true);

        when(jwtUtil.generateToken(any(User.class)))
                .thenReturn("token123");

        LoginRequest request = new LoginRequest();
        request.setEmail("user@mail.com");
        request.setPassword("123456");
        RefreshToken mockRefresh = new RefreshToken();
mockRefresh.setToken("refresh123");

when(refreshTokenService.create(anyLong()))
        .thenReturn(mockRefresh);

        AuthResponse response = authService.login(request);

        assertEquals("token123", response.getToken());
    }

    @Test
    void verifyEmail_ShouldVerifyUser() {

        User user = new User();
        user.setEmail("user@mail.com");
        user.setVerificationCode("code123");
        user.setEmailVerified(false);

        when(userRepository.findByVerificationCode("code123"))
                .thenReturn(Optional.of(user));

        authService.verifyEmail("code123");

        assertTrue(user.isEmailVerified());

        verify(userRepository).save(user);
    }

    @Test
    void changeEmail_ShouldUpdateEmailAndSendEvent() {

        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");

        when(userRepository.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(user));

        authService.changeEmail("new@mail.com");

        assertEquals("new@mail.com", user.getEmail());

        verify(userRepository).save(user);

        verify(rabbitTemplate).convertAndSend(
                eq("user.exchange"),
                eq("user.email.changed"),
                any(UserEmailChangedEvent.class)
        );
    }

    @Test
    void changePassword_ShouldUpdatePassword() {

        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword("old");

        when(userRepository.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newPass"))
                .thenReturn("newEncoded");

        authService.changePassword("newPass");

        assertEquals("newEncoded", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ShouldUpdateUserAndReturnToken() {

        User user = new User();
        user.setId(1L);
        user.setEmail("old@mail.com");
        user.setRol(Role.USER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(any(User.class)))
                .thenReturn("token123");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);
        request.setEmail("new@mail.com");
        request.setRole(Role.ADMIN);

        AuthResponse response = authService.updateUser(request);

        assertEquals("token123", response.getToken());
        assertEquals("new@mail.com", user.getEmail());
        assertEquals(Role.ADMIN, user.getRol());

        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_ShouldDeleteUser() {

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        String result = authService.deleteUser(1L);

        assertEquals("Usuario de auth eliminado con éxito", result);

        verify(userRepository).delete(user);
    }
}