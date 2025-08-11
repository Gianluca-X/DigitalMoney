package com.example.userservice;

import com.example.userservice.config.jwt.TokenManager;
import com.example.userservice.dto.entry.AccountCreationRequest;
import com.example.userservice.dto.entry.AccountResponse;
import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.entry.UserRegisterRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void deberiaDeRegistrarUnUsuario() {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(
                "Sebastian0", "Sanchez", "33240969", "1135075158",
                "palacios2@gmail.com", 21L);

        when(userRepository.existsByEmail(userRegisterRequest.getEmail())).thenReturn(false);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("Sebastian");

        // Solo mantenemos el stub que realmente se usa
        lenient().when(userRepository.save(any(User.class))).thenReturn(mockUser);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        lenient().when(userRepository.existsByEmail(userRegisterRequest.getEmail())).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        AccountResponse mockAccountResponse = new AccountResponse();
        mockAccountResponse.setId(123L);
        when(accountClient.createAccount(any(AccountCreationRequest.class)))
                .thenReturn(mockAccountResponse);

        User result = userService.createUserFromEvent(userRegisterRequest);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Sebastian", result.getFirstName());

        verify(userRepository, times(2)).save(any(User.class));
        verify(accountClient).createAccount(any(AccountCreationRequest.class));
    }

}
