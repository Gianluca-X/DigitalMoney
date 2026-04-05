package com.example.authservice.controller;
import com.example.authservice.entity.Role;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.UserEntry;
import com.example.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.example.authservice.service.RefreshTokenService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private Authentication authentication;
@Mock
RefreshTokenService refreshTokenService;
    @InjectMocks private AuthController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("user@mail.com");
    }

    @Test
    void register_returnsAuthResponse() {
        UserEntry entry = new UserEntry();
entry.setEmail("nerea@example.com");
entry.setPassword("clave123");
entry.setRol(Role.USER); // Ajusta según tu enum
        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAuthId(1L);

        when(authService.register(entry)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = controller.register(entry);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getAuthId());
    }

    @Test
void login_returnsToken() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("nerea@example.com");
    loginRequest.setPassword("clave123");

    // Mock del AuthResponse que devuelve authService
    AuthResponse mockResponse = new AuthResponse();
    mockResponse.setToken("jwt-token");
    mockResponse.setAuthId(1L);

    when(authService.login(loginRequest)).thenReturn(mockResponse);

    ResponseEntity<AuthResponse> response = controller.login(loginRequest);

    assertEquals(200, response.getStatusCodeValue());
    assertNotNull(response.getBody());
    assertEquals("jwt-token", response.getBody().getToken());  // CORRECTO
    assertEquals(1L, response.getBody().getAuthId());           // opcional
}
    @Test
    void changeEmail_success() {
        ResponseEntity<String> response = controller.changeEmail("nuevo@example.com", authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Email updated successfully", response.getBody());
        verify(authService).changeEmail("nuevo@example.com");
    }

    @Test
    void changePassword_success() {
        ResponseEntity<String> response = controller.changePassword("nuevaClave");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password updated successfully", response.getBody());
        verify(authService).changePassword("nuevaClave");
    }

    @Test
    void verify_success() {
        ResponseEntity<String> response = controller.verify("codigo-verificacion");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("✅ Email verified successfully", response.getBody());
        verify(authService).verifyEmail("codigo-verificacion");
    }
}
