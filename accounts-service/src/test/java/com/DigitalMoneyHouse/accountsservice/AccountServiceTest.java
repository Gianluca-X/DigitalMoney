package com.DigitalMoneyHouse.accountsservice;

import com.DigitalMoneyHouse.accountsservice.dto.AccountCreationRequest;
import com.DigitalMoneyHouse.accountsservice.dto.AccountResponse;
import com.DigitalMoneyHouse.accountsservice.dto.exit.AccountOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.AccountRequest;
import com.DigitalMoneyHouse.accountsservice.entities.Transaction;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.TransactionRepository;
import com.DigitalMoneyHouse.accountsservice.service.impl.AccountsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountsRepository accountsRepository;

    @InjectMocks
    private AccountsServiceImpl accountsService;
    @Mock
    private ModelMapper modelMapper; // Mock del ModelMapper
    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void deberiaDeCrearUnaCuentaExitosamente() {
        // Configuración del ModelMapper mockeado
        AccountResponse accountResponseMock = new AccountResponse();
        accountResponseMock.setId(1L);
        accountResponseMock.setBalance(BigDecimal.valueOf(1000));

        Account accountMock = new Account();
        accountMock.setId(1L);
        accountMock.setBalance(BigDecimal.valueOf(1000));

        when(accountsRepository.save(any(Account.class))).thenReturn(accountMock);

        AccountCreationRequest request = AccountCreationRequest.builder()
                .userId(1L)
                .email("matiazep2@gmail.com")
                .alias("alias.prueba.test")
                .cvu("6784193874263590167412")
                .initialBalance(BigDecimal.valueOf(1000))
                .build();

        // Act
        AccountResponse response = accountsService.createAccount(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());
    }
    @Test
    void deberiaObtenerTodasLasCuentas() {
        // Datos de prueba: Lista de cuentas simuladas
        Account account1 = new Account();
        account1.setId(1L);
        account1.setBalance(new BigDecimal("1000.00")); // Usar BigDecimal en lugar de double

        Account account2 = new Account();
        account2.setId(2L);
        account2.setBalance(new BigDecimal("1500.00")); // Usar BigDecimal en lugar de double

        List<Account> mockAccounts = Arrays.asList(account1, account2);

        // Mock del comportamiento del repositorio para devolver las cuentas simuladas
        when(accountsRepository.findAll()).thenReturn(mockAccounts);

        // Mock del comportamiento del ModelMapper para convertir Account a AccountOutDTO
        AccountOutDTO accountOutDTO1 = new AccountOutDTO();
        accountOutDTO1.setId(1L);
        accountOutDTO1.setBalance(new BigDecimal("1000.00")); // Usar BigDecimal

        AccountOutDTO accountOutDTO2 = new AccountOutDTO();
        accountOutDTO2.setId(2L);
        accountOutDTO2.setBalance(new BigDecimal("1500.00")); // Usar BigDecimal

        when(modelMapper.map(account1, AccountOutDTO.class)).thenReturn(accountOutDTO1);
        when(modelMapper.map(account2, AccountOutDTO.class)).thenReturn(accountOutDTO2);

        // Ejecución del método a probar
        List<AccountOutDTO> result = accountsService.getAccounts();

        // Verificación: Comprobamos que la lista de DTOs tiene los valores correctos
        assertEquals(2, result.size(), "El tamaño de la lista de cuentas debería ser 2");
        assertEquals(accountOutDTO1.getId(), result.get(0).getId());
        assertEquals(accountOutDTO2.getBalance(), result.get(1).getBalance(), "Los balances deben coincidir");

        // Verificación: Se llamó al repositorio para obtener las cuentas
        verify(accountsRepository).findAll();

        // Verificación: Se llamaron las conversiones correctas del ModelMapper
        verify(modelMapper).map(account1, AccountOutDTO.class);
        verify(modelMapper).map(account2, AccountOutDTO.class);
    }
    @Test
    void deberiaDevolverUnaCuentaPorIdComoAccountOutDTOCuandoEstaExista() throws ResourceNotFoundException {
        // Arrange
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("1000.00"));

        // Simula el comportamiento de findById() en el repositorio
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Configura el mock del ModelMapper para que convierta el Account a AccountOutDTO
        AccountOutDTO accountOutDTO = new AccountOutDTO();
        accountOutDTO.setId(accountId);
        accountOutDTO.setBalance(new BigDecimal("1000.00"));
        when(modelMapper.map(account, AccountOutDTO.class)).thenReturn(accountOutDTO);

        // Act
        AccountOutDTO result = accountsService.getAccountById(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getId());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());

        // Verifica que findById fue llamado con el accountId
        verify(accountsRepository).findById(accountId);

        // Verifica que modelMapper fue llamado para convertir el Account a AccountOutDTO
        verify(modelMapper).map(account, AccountOutDTO.class);
    }
    @Test
    void deberiDeObtenerLasUltimas5TransaccionesCuandoEstasExistan() {
        // Arrange
        Long accountId = 1L;

        // Creamos algunas transacciones de prueba
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setAccountId(accountId);
        transaction1.setAmount(100.00);
        transaction1.setDate(LocalDateTime.now().minusDays(1));

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAccountId(accountId);
        transaction2.setAmount(200.00);
        transaction2.setDate(LocalDateTime.now().minusDays(2));

        // Simulamos el comportamiento de findTop5ByAccountIdOrderByDateDesc() en el repositorio
        when(transactionRepository.findTop5ByAccountIdOrderByDateDesc(accountId))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        // Act
        List<Transaction> result = accountsService.getLastTransactions(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Solo tenemos 2 transacciones, no 5
        assertEquals(transaction1.getId(), result.get(0).getId());
        assertEquals(transaction2.getAmount(), result.get(1).getAmount());

        // Verifica que el método del repositorio fue llamado correctamente
        verify(transactionRepository).findTop5ByAccountIdOrderByDateDesc(accountId);
    }
    @Test
    void deberiaDeObtenerElBalanceDeUnaCuantaPorIdCuandoEstaExista() throws ResourceNotFoundException {
        // Arrange
        Long accountId = 1L;
        Account accountMock = new Account();
        accountMock.setId(accountId);
        accountMock.setBalance(new BigDecimal("1500.00"));

        // Simular que el repositorio devuelve una cuenta con el ID especificado
        when(accountsRepository.findById(accountId)).thenReturn(java.util.Optional.of(accountMock));

        // Act
        AccountResponse result = accountsService.getAccountSummary(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getId());
        assertEquals(accountMock.getBalance(), result.getBalance());

        // Verificar que el repositorio se haya llamado correctamente
        verify(accountsRepository).findById(accountId);
    }
}

