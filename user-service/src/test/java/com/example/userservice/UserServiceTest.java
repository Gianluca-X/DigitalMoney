package com.example.userservice;

import com.example.userservice.config.jwt.TokenManager;
import com.example.userservice.dto.entry.AccountCreationRequest;
import com.example.userservice.dto.entry.AccountResponse;
import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.impl.EmailService;
import com.example.userservice.service.impl.TokenBlacklistService;
import com.example.userservice.service.impl.UserServiceImpl;
import com.example.userservice.service.client.AccountClient;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en pruebas
public class UserServiceTest {

    @Mock
    private UserRepository userRepository; // Mock del repositorio

    @Mock
    private ModelMapper modelMapper; // Mock del ModelMapper

    @Mock
    private PasswordEncoder passwordEncoder; // Mock del PasswordEncoder

    @Mock
    private AccountClient accountClient; // Mock del AccountClient

    @InjectMocks
    private UserServiceImpl userService; // Servicio a probar

    @Mock
    private TokenManager tokenManager; // Mock del JwtProvider

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void deberiaDeRegistrarUnUsuario() {
        // Datos de entrada
        UserEntryDto userEntryDto = new UserEntryDto(
                "Sebastian", "Sanchez", "33240969", "1135075158",
                "palacios2@gmail.com", "RiverPlate2024", new HashSet<>()
        );

        // Mock del comportamiento de PasswordEncoder
        when(passwordEncoder.encode(Mockito.anyString())).thenReturn("hashedPassword");

        // Mock del comportamiento del repositorio
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("Sebastian");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Mock del comportamiento de ModelMapper
        when(modelMapper.map(any(UserEntryDto.class), Mockito.eq(User.class))).thenReturn(mockUser);

        // Simulación del contexto de seguridad con un token
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getCredentials()).thenReturn("mocked-jwt-token");
        SecurityContextHolder.setContext(securityContext);

        AccountResponse mockAccountResponse = new AccountResponse();
        mockAccountResponse.setId(123L);  // Asumiendo que el AccountResponse tiene un ID

        when(accountClient.createAccount(any(AccountCreationRequest.class), any(String.class)))
                .thenReturn(mockAccountResponse);

        // Ejecución
        User result = userService.createUser(userEntryDto);

        // Verificación
        assertNotNull(result.getId());
        assertEquals("Sebastian", result.getFirstName());

        // Verifica que el método save fue llamado dos veces
        Mockito.verify(userRepository, Mockito.times(2)).save(any(User.class));  // Cambia a 2 veces
        Mockito.verify(passwordEncoder).encode(Mockito.anyString());
        Mockito.verify(accountClient).createAccount(any(AccountCreationRequest.class), any(String.class));
    }

    // ... Resto de los tests sin cambios ...
}
