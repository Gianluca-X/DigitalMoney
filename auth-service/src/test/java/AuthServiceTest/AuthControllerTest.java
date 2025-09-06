package com.example.authservice.controller;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.UserEntry;
import com.example.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private Authentication authentication;

    @InjectMocks private AuthController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("user@mail.com");
    }

    @Test
    void register_returnsAuthResponse() {
        UserEntry entry = new UserEntry("Nerea", "Martínez", "11223344", "22334455", "nerea@example.com", "clave123");
        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAuthId(1L);
        mockResponse.setToken("jwt-token");

        when(authService.register(entry)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = controller.register(entry);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getAuthId());
        assertEquals("jwt-token", response.getBody().getToken());
    }

    @Test
    void login_returnsToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nerea@example.com");
        loginRequest.setPassword("clave123");

        when(authService.login(loginRequest)).thenReturn("jwt-token");

        ResponseEntity<String> response = controller.login(loginRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("jwt-token"));
        assertTrue(response.getBody().contains("Login Exitoso"));
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
        assertEquals("Email verified successfully", response.getBody());
        verify(authService).verifyEmail("codigo-verificacion");
    }
}
