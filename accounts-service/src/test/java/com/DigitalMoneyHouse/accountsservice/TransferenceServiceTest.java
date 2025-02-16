package com.DigitalMoneyHouse.accountsservice;

import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferRequestOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferenceOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Activity;
import com.DigitalMoneyHouse.accountsservice.entities.Card;
import com.DigitalMoneyHouse.accountsservice.entities.Transference;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.ActivityRepository;
import com.DigitalMoneyHouse.accountsservice.repository.CardRepository;
import com.DigitalMoneyHouse.accountsservice.repository.TransferenceRepository;
import com.DigitalMoneyHouse.accountsservice.service.impl.TransferenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class TransferenceServiceTest {
    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransferenceRepository transferenceRepository;

    @Mock
    private ActivityRepository activityRepository;
    @InjectMocks
    private TransferenceServiceImpl transferenceServiceImpl;

    private Account account;
    private Card card;
    private TransferenceOutDTO transferenceOutDto;
    private Account senderAccount;
    private Account recipientAccount;
    private TransferRequestOutDTO transferRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCvu("1234567890");

        card = new Card();
        card.setId(1L);
        card.setNumber("1111222233334444");

        transferenceOutDto = new TransferenceOutDTO();
        transferenceOutDto.setCardId(1L);


        //test para transferencia en Efectivo
        // Cuenta remitente
        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setBalance(new BigDecimal("1000.00"));
        senderAccount.setCvu("1234567890");

        // Cuenta destinatario
        recipientAccount = new Account();
        recipientAccount.setId(2L);
        recipientAccount.setBalance(new BigDecimal("500.00"));
        recipientAccount.setCvu("0987654321");

        // Datos de la transferencia
        transferRequest = new TransferRequestOutDTO();
        transferRequest.setAmount(new BigDecimal("200.00"));
        transferRequest.setRecipient("0987654321");
    }

    @Test
    public void deberiaDeHacerUnaTransferenciaMedianteTarjeta() throws Exception {
        // Arrange
        when(accountsRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Act
        transferenceServiceImpl.registerTransferenceFromCards(1L, transferenceOutDto);

        // Assert
        verify(transferenceRepository, times(1)).save(any(Transference.class));
        verify(accountsRepository, times(1)).save(any(Account.class));
        verify(activityRepository, times(1)).save(any(Activity.class));
        assertEquals(new BigDecimal("1500.00"), account.getBalance()); // Verifica el balance actualizado
    }
    @Test
    public void deberiaDeHacerUnaTransferenciaMedianteDineroEnEfectivo() throws Exception {
        // Arrange: Simulamos la cuenta del remitente
        when(accountsRepository.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountsRepository.findById(2L)).thenReturn(Optional.of(recipientAccount));

        // Simulamos la búsqueda de la cuenta del destinatario
        when(accountsRepository.findByAlias(transferRequest.getRecipient())).thenReturn(null); // No encontrado por alias
        when(accountsRepository.findByCvu(transferRequest.getRecipient())).thenReturn(recipientAccount); // Encontrado por CVU

        // Simulamos el comportamiento del repositorio y la creación de los objetos
        when(accountsRepository.save(any(Account.class))).thenReturn(senderAccount, recipientAccount);
        when(transferenceRepository.save(any(Transference.class))).thenReturn(new Transference());
        when(activityRepository.save(any(Activity.class))).thenReturn(new Activity());

        // Act: Ejecutamos la transferencia
        transferenceServiceImpl.makeTransferFromCash(1L, transferRequest);

        // Assert: Verificamos que las cuentas fueron actualizadas correctamente
        verify(accountsRepository, times(2)).save(any(Account.class)); // Ambas cuentas deben ser guardadas
        verify(transferenceRepository, times(1)).save(any(Transference.class)); // La transferencia debe guardarse
        verify(activityRepository, times(2)).save(any(Activity.class)); // Ambas actividades deben registrarse
        assertEquals(new BigDecimal("800.00"), senderAccount.getBalance()); // Verifica el balance actualizado del remitente
        assertEquals(new BigDecimal("700.00"), recipientAccount.getBalance()); // Verifica el balance actualizado del destinatario
    }
    @Test
    public void DeberiaDeObtenerLasUltimasTransferencias() {
        // Arrange: Crear algunas transferencias simuladas
        Transference transfer1 = new Transference();
        transfer1.setAccountId(1L);
        transfer1.setAmount(new BigDecimal("100.00"));
        transfer1.setDate(LocalDateTime.now().minusDays(1));

        Transference transfer2 = new Transference();
        transfer2.setAccountId(1L);
        transfer2.setAmount(new BigDecimal("200.00"));
        transfer2.setDate(LocalDateTime.now().minusDays(2));

        // Lista con las transferencias simuladas
        List<Transference> transferList = Arrays.asList(transfer1, transfer2);

        // Simular que el repositorio devuelve las transferencias para la cuenta 1L
        when(transferenceRepository.findTop5ByAccountIdOrderByDateDesc(1L)).thenReturn(transferList);

        // Act: Llamar al método del servicio
        List<Transference> result = transferenceServiceImpl.getLastTransferredAccounts(1L);

        // Assert: Verificar que el servicio devolvió la lista correcta
        assertNotNull(result); // Verifica que la lista no sea nula
        assertEquals(2, result.size()); // Verifica que la lista contiene 2 transferencias
        assertEquals(new BigDecimal("100.00"), result.get(0).getAmount()); // Verifica el monto de la primera transferencia
        assertEquals(new BigDecimal("200.00"), result.get(1).getAmount()); // Verifica el monto de la segunda transferencia

        // Verificar que el repositorio fue llamado correctamente
        verify(transferenceRepository, times(1)).findTop5ByAccountIdOrderByDateDesc(1L);
    }

}
