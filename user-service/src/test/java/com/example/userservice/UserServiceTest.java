package com.example.userservice;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.entry.AccountCreationRequest;
import com.example.userservice.dto.entry.AccountResponse;
import com.example.userservice.dto.entry.UserRegisterRequest;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import com.example.userservice.exceptions.EmailAlreadyRegisteredException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.client.AccountClient;
import com.example.userservice.service.client.AuthClient;
import com.example.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AccountClient accountClient;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void deberiaRegistrarUnUsuario() {
        // ARRANGE
        UserRegisterRequest request = new UserRegisterRequest(
                "palacios2@gmail.com", // email
                "Sebastian",           // firstName
                "Sanchez",             // lastName
                "1135075158",          // phone
                "33240969",            // dni
                "password123"          // password
        );


        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        AuthResponse mockAuthResponse = new AuthResponse();
        mockAuthResponse.setAuthId(1L);
        mockAuthResponse.setToken("fake-jwt-token");
        when(authClient.registerUser(any())).thenReturn(mockAuthResponse);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("Sebastian");
        savedUser.setEmail(request.getEmail());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(123L);
        accountResponse.setBalance(BigDecimal.ZERO);
        when(accountClient.createAccount(any(AccountCreationRequest.class)))
                .thenReturn(accountResponse);

        // ACT
        UserRegisterOutDto result = userService.handleRegister(request);

        // ASSERT
        assertNotNull(result);
        assertEquals("Sebastian", result.getFirstName());
        assertEquals("palacios2@gmail.com", result.getEmail());
        assertEquals(1L, result.getAuthId());
        assertEquals(123L, result.getAccountId());

        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(accountClient).createAccount(any(AccountCreationRequest.class));
        verify(authClient).registerUser(any());
    }

    @Test
    void deberiaLanzarExcepcionSiEmailYaExiste() {
        // ARRANGE
        UserRegisterRequest request = new UserRegisterRequest(
                "Sebastian", "Sanchez", "33240969", "1135075158",
                "palacios2@gmail.com", "password123"
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // ACT + ASSERT
        assertThrows(EmailAlreadyRegisteredException.class,
                () -> userService.handleRegister(request));

        verify(userRepository, never()).save(any(User.class));
        verify(authClient, never()).registerUser(any());
        verify(accountClient, never()).createAccount(any());
    }
}
