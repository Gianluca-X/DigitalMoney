package com.example.userservice;

import com.example.userservice.config.jwt.TokenManager;
import com.example.userservice.dto.entry.AccountResponse;
import com.example.userservice.dto.entry.LoginRequest;
import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.exceptions.IncorrectPasswordException;
import com.example.userservice.exceptions.EmailNotVerifiedException;
import com.example.userservice.exceptions.UserNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;
import java.util.Optional;

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
    @Mock
    private EmailService emailService;

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

        AccountResponse mockAccountResponse = new AccountResponse();
        mockAccountResponse.setId(123L);  // Asumiendo que el AccountResponse tiene un ID

        when(accountClient.createAccount(any())).thenReturn(mockAccountResponse);

        // Ejecución
        User result = userService.createUser(userEntryDto);

        // Verificación
        assertNotNull(result.getId());
        assertEquals("Sebastian", result.getFirstName());

        // Verifica que el método save fue llamado dos veces
        Mockito.verify(userRepository, Mockito.times(2)).save(any(User.class));  // Cambia a 2 veces
        Mockito.verify(passwordEncoder).encode(Mockito.anyString());
        Mockito.verify(accountClient).createAccount(any()); // Verifica la llamada a accountClient
    }
    @Test
    void deberiaAutenticarYGenerarTokenExitosamente() throws UserNotFoundException, IncorrectPasswordException, EmailNotVerifiedException {
        // Datos de entrada para la prueba
        LoginRequest loginRequest = new LoginRequest("palacios2@gmail.com", "RiverPlate2024");

        // Datos de usuario simulados
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("palacios2@gmail.com");
        mockUser.setPassword("$2a$10$7VyxHOsab0I7c6XGfaQ.1.Afrf5iXzHaR2OHDH28yA6ZdksNl2gkS"); // Contraseña encriptada
        mockUser.setEmailVerified(true); // Simula que el correo está verificado

        // Mock del comportamiento del repositorio para encontrar el usuario
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));

        // Mock del comportamiento del PasswordEncoder para verificar la contraseña
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);

        // Mock del comportamiento del JwtProvider para generar el token
        String expectedToken = "some-jwt-token";
        when(tokenManager.createToken(mockUser.getId(), mockUser.getEmail())).thenReturn(expectedToken);

        // Ejecución
        String actualToken = userService.authenticateAndLogin(loginRequest);

        // Verificación de que el token generado es el esperado
        assertEquals(expectedToken, actualToken);

        // Verificación de que se hayan llamado los métodos correctos
        Mockito.verify(userRepository).findByEmail(loginRequest.getEmail());
        Mockito.verify(passwordEncoder).matches(loginRequest.getPassword(), mockUser.getPassword());
        Mockito.verify(tokenManager).createToken(mockUser.getId(), mockUser.getEmail());
    }
    @Test
    void deberiaEliminarUsuario() {
        // ID del usuario a eliminar
        Long userId = 4L;

        // Ejecución
        userService.deleteUser(userId);

        // Verificación de que el método deleteById fue llamado con el ID correcto
        Mockito.verify(userRepository).deleteById(userId);
    }

    @Test
    void deberiaObtenerUsuarioPorId() {
        // Datos de entrada
        Long userId = 1L;

        // Usuario simulado
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setFirstName("Sebastian");
        mockUser.setEmail("palacios2@gmail.com");

        // Usuario DTO esperado
        UserRegisterOutDto expectedUserDto = new UserRegisterOutDto();
        expectedUserDto.setId(userId);
        expectedUserDto.setFirstName("Sebastian");
        expectedUserDto.setEmail("palacios2@gmail.com");

        // Mock del comportamiento del repositorio
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock del comportamiento del ModelMapper
        when(modelMapper.map(any(User.class), Mockito.eq(UserRegisterOutDto.class))).thenReturn(expectedUserDto);

        // Ejecución
        UserRegisterOutDto actualUserDto = userService.getUserById(userId);

        // Verificaciones
        assertNotNull(actualUserDto); // Verifica que el DTO no sea nulo
        assertEquals(expectedUserDto.getId(), actualUserDto.getId()); // Verifica que el ID coincida
        assertEquals(expectedUserDto.getFirstName(), actualUserDto.getFirstName()); // Verifica que el nombre coincida
        assertEquals(expectedUserDto.getEmail(), actualUserDto.getEmail()); // Verifica que el correo coincida

        // Verifica que se haya llamado al repositorio para obtener el usuario
        Mockito.verify(userRepository).findById(userId);
    }
    @Test
    void deberiaCerrarSesionCorrectamente() {
        // Token de prueba
        String token = "Bearer some-jwt-token";

        // Ejecución
        userService.logoutUser(token);

        // Verificación: Se verifica que el método invalidateToken haya sido llamado una vez con el token sin el prefijo "Bearer "
        Mockito.verify(tokenBlacklistService).invalidateToken("some-jwt-token");
    }
    @Test
    void deberiaDeVerificarElRegistroDeUnUsuarioMedianteElEmailyUnCodigoDeValidacion() {
        // Datos de prueba
        String email = "matiazep2@example.com";
        String validVerificationCode = "123456";

        // Configuración del usuario simulado
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setVerificationCode(validVerificationCode);
        user.setEmailVerified(false);

        // Mock del repositorio
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Llamada al método a probar
        userService.verifyUserEmail(email, validVerificationCode);

        // Verificaciones
        assertTrue(user.isEmailVerified(), "El email debe estar verificado");
        assertNull(user.getVerificationCode(), "El código de verificación debe ser null");

        // Verificar interacciones con el repositorio
        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(userRepository).save(user);
    }
    @Test
    void deberiaDeEmpezarElProcesoParaCambiarLaContraseñaMedianteElEnvioDeUnEmailConElTokenyRedirigirHaciaLaPaginaDelFrontEndParaConfirmarElCambio() {
        // Datos de prueba
        String email = "matiazep2@gmail.com";
        String token = "mocked-jwt-token";
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        // Configuración de mocks
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(tokenManager.createToken(user.getId(), user.getEmail())).thenReturn(token);

        // Llamada al método
        userService.processPasswordResetRequest(email);

        // Verificaciones
        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(tokenManager).createToken(user.getId(), user.getEmail());
        Mockito.verify(emailService).sendEmail(email,
                "Recuperación de la contraseña",
                "Haz clic en el siguiente enlace para restablecer tu contraseña: " + resetLink);
    }
    @Test
    void deberiaDeConfirmarElCambioDeLaNuevaContraseña() {
        // Datos de prueba
        String token = "mocked-jwt-token";
        String newPassword = "NewPassword123";
        String confirmPassword = "NewPassword123";
        String email = "matiazep2@gmail.com";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        // Configuración de mocks
        Mockito.when(tokenManager.verifyToken(token)).thenReturn(email);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Llamada al método
        userService.resetPassword(token, newPassword, confirmPassword);

        // Verificaciones
        Mockito.verify(tokenManager).verifyToken(token);
        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(userRepository).save(user);

        // Asegurar que la contraseña se haya actualizado
        assertNotEquals(newPassword, user.getPassword(), "La contraseña debe estar encriptada");
    }

}
