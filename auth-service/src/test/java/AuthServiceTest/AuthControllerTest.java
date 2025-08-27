package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.UserEntry;
import com.example.authservice.entity.User;
import com.example.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController controller = new AuthController(authService);

    @Test
    void register_returnsToken() {
        UserEntry entry = new UserEntry("Nerea", "Martínez", "11223344", "22334455", "nerea@example.com", "clave123");
        when(authService.register(entry)).thenReturn("jwt-token");

        ResponseEntity<String> response = controller.register(entry);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("jwt-token", response.getBody());
    }

    @Test
    void login_returnsToken() {
        LoginRequest user = new LoginRequest();
        user.setEmail("nerea@example.com");
        user.setPassword("clave123");

        when(authService.login(user)).thenReturn("jwt-token");

        ResponseEntity<String> response = controller.login(user);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("jwt-token", response.getBody());
    }

    @Test
    void changeEmail_success() {
        ResponseEntity<String> response = controller.changeEmail( "nuevo@example.com",2333232);
        assertEquals(200, response.getStatusCodeValue());
        verify(authService).changeEmail("nuevo@example.com",);
    }

    @Test
    void changePassword_success() {
        ResponseEntity<String> response = controller.changePassword(1L, "nuevaClave");
        assertEquals(200, response.getStatusCodeValue());
        verify(authService).changePassword(1L, "nuevaClave");
    }

    @Test
    void verify_success() {
        ResponseEntity<String> response = controller.verify("codigo-verificacion");
        assertEquals(200, response.getStatusCodeValue());
        verify(authService).verifyEmail("codigo-verificacion");
    }
}
