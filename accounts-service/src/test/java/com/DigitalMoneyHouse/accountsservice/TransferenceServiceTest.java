package com.DigitalMoneyHouse.accountsservice;

import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferRequestOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferenceOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.*;
import com.DigitalMoneyHouse.accountsservice.repository.*;
import com.DigitalMoneyHouse.accountsservice.service.impl.TransferenceServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(account, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        card = new Card();
        card.setId(1L);
        card.setAccountId(1L);

        transferenceOutDto = new TransferenceOutDTO();
        transferenceOutDto.setCardId(1L);
        transferenceOutDto.setAmount(new BigDecimal("500.00"));

        senderAccount = account;

        recipientAccount = new Account();
        recipientAccount.setId(2L);
        recipientAccount.setBalance(new BigDecimal("500.00"));
        recipientAccount.setCvu("0987654321");

        transferRequest = new TransferRequestOutDTO();
        transferRequest.setAmount(new BigDecimal("200.00"));
        transferRequest.setRecipient("0987654321");
    }

    @Test
    public void deberiaDepositarConTarjeta() {

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        transferenceServiceImpl.registerTransferenceFromCards(transferenceOutDto);

        verify(transferenceRepository).save(any(Transference.class));
        verify(accountsRepository).save(any(Account.class));
        verify(activityRepository).save(any(Activity.class));

        assertEquals(new BigDecimal("1500.00"), account.getBalance());
    }

    @Test
    public void deberiaTransferirDinero() {

        when(accountsRepository.findByIdForUpdate(senderAccount.getId()))
                .thenReturn(Optional.of(senderAccount));

        when(accountsRepository.findByAlias(transferRequest.getRecipient()))
                .thenReturn(null);

        when(accountsRepository.findByCvu(transferRequest.getRecipient()))
                .thenReturn(recipientAccount);

        when(accountsRepository.save(any(Account.class)))
                .thenReturn(senderAccount, recipientAccount);

        transferenceServiceImpl.makeTransferFromCash(transferRequest);

        assertEquals(new BigDecimal("800.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), recipientAccount.getBalance());
    }

    @Test
    public void deberiaObtenerUltimasTransferencias() {

        Transference t1 = new Transference();
        t1.setAmount(new BigDecimal("100"));

        Transference t2 = new Transference();
        t2.setAmount(new BigDecimal("200"));

        when(transferenceRepository.findTop5ByAccountIdOrderByDateDesc(1L))
                .thenReturn(Arrays.asList(t1, t2));

        List<Transference> result =
                transferenceServiceImpl.getLastTransferredAccounts(1L);

        assertEquals(2, result.size());
    }
    @Test
public void deberiaSoportarConcurrenciaSinPerderDinero() throws InterruptedException {

    int threads = 5;

    ExecutorService executor = Executors.newFixedThreadPool(threads);

    when(accountsRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(senderAccount));

    when(accountsRepository.findByAlias(any())).thenReturn(null);
    when(accountsRepository.findByCvu(any())).thenReturn(recipientAccount);

    when(accountsRepository.save(any(Account.class)))
            .thenReturn(senderAccount, recipientAccount);

    when(transferenceRepository.save(any())).thenReturn(new Transference());
    when(activityRepository.save(any())).thenReturn(new Activity());

    CountDownLatch latch = new CountDownLatch(threads);

    for (int i = 0; i < threads; i++) {

        executor.submit(() -> {

            try {
                // 🔥 CLAVE: setear contexto en cada hilo
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(senderAccount, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);

                transferenceServiceImpl.makeTransferFromCash(transferRequest);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await();
    executor.shutdown();

    // 🔥 5 transferencias de 1000 -> debería quedar 0
    assertEquals(new BigDecimal("0.00"), senderAccount.getBalance());
}
}