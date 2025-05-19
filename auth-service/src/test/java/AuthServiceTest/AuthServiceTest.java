
import com.example.authservice.dto.UserEmailChangedEvent;
import com.example.authservice.dto.UserEntry;
import com.example.authservice.dto.UserRegisterRequest;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;

import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

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
    }

    @Test
    void register_ShouldRegisterUserAndSendEmailAndPublishEvent() {
        // Arrange
        UserEntry entry = new UserEntry("nerea@mail.com", "123456", "Nerea", "Martínez", "12345678", "1130000000");
        User user = new User();
        user.setId(1L);
        user.setEmail(entry.getEmail());

        when(passwordEncoder.encode(entry.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(entry.getEmail())).thenReturn("mocked-token");

        // Act
        String token = authService.register(entry);

        // Assert
        assertEquals("mocked-token", token);
        verify(emailService).sendVerificationEmail(eq(entry.getEmail()), any(String.class));
        verify(userEventPublisher).publishRegisterEvent(any(UserRegisterRequest.class));
    }

    @Test
    void login_ShouldReturnTokenIfCredentialsAreValid() {
        // Arrange
        User loginUser = new User();
        loginUser.setEmail("test@mail.com");
        loginUser.setPassword("1234");

        User userFromDb = new User();
        userFromDb.setEmail("test@mail.com");
        userFromDb.setPassword("hashed");
        userFromDb.setEmailVerified(true);

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(userFromDb));
        when(passwordEncoder.matches("1234", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("test@mail.com")).thenReturn("token123");

        // Act
        String token = authService.login(loginUser);

        // Assert
        assertEquals("token123", token);
    }

    @Test
    void login_ShouldThrowIfEmailNotVerified() {
        User loginUser = new User();
        loginUser.setEmail("test@mail.com");
        loginUser.setPassword("1234");

        User userFromDb = new User();
        userFromDb.setPassword("hashed");
        userFromDb.setEmailVerified(false);

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(userFromDb));
        when(passwordEncoder.matches("1234", "hashed")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.login(loginUser));
    }

    @Test
    void changeEmail_ShouldUpdateEmailAndPublishEvent() {
        User user = new User();
        user.setId(1L);
        user.setEmail("old@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.changeEmail(1L, "new@mail.com");

        verify(userRepository).save(user);
        verify(rabbitTemplate).convertAndSend(eq("user.exchange"), eq("user.email.changed"), ArgumentMatchers.<UserEmailChangedEvent>any());
    }

    @Test
    void changePassword_ShouldUpdatePassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("old");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");

        authService.changePassword(1L, "newPass");

        assertEquals("newEncoded", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_ShouldSetEmailVerifiedToTrue() {
        User user = new User();
        user.setEmail("verified@mail.com");
        user.setEmailVerified(false);

        when(userRepository.findByEmail("verified@mail.com")).thenReturn(Optional.of(user));

        authService.verifyEmail("verified@mail.com");

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
    }
}
