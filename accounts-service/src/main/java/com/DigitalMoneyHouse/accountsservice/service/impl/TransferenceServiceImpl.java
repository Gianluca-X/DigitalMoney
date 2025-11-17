package com.DigitalMoneyHouse.accountsservice.service.impl;

import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferRequestOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferenceOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Activity;
import com.DigitalMoneyHouse.accountsservice.entities.Card;
import com.DigitalMoneyHouse.accountsservice.entities.Transference;
import com.DigitalMoneyHouse.accountsservice.exceptions.BadRequestException;
import com.DigitalMoneyHouse.accountsservice.exceptions.InsufficientFundsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.exceptions.UnauthorizedException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.ActivityRepository;
import com.DigitalMoneyHouse.accountsservice.repository.CardRepository;
import com.DigitalMoneyHouse.accountsservice.repository.TransferenceRepository;
import com.DigitalMoneyHouse.accountsservice.service.ITransferenceService;
import jakarta.ws.rs.core.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferenceServiceImpl implements ITransferenceService {

    @Autowired
    private TransferenceRepository transferenceRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ActivityRepository activityRepository;

    public TransferenceServiceImpl(TransferenceRepository transferenceRepository, AccountsRepository accountsRepository, CardRepository cardRepository, ActivityRepository activityRepository) {
        this.transferenceRepository = transferenceRepository;
        this.accountsRepository = accountsRepository;
        this.cardRepository = cardRepository;
        this.activityRepository = activityRepository;
    }
    public void registerTransferenceFromCards(Long accountId, TransferenceOutDTO transferenceOutDto)
            throws ResourceNotFoundException, UnauthorizedException {

        Account account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Card card = cardRepository.findById(transferenceOutDto.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        // Validar que la tarjeta pertenece a la cuenta
        if (!card.getAccountId().equals(account.getId())) {
            throw new UnauthorizedException("No puedes usar una tarjeta que no corresponde a tu cuenta.");
        }

        Transference transference = new Transference();
        transference.setAccountId(account.getId());
        transference.setCardId(card.getId());
        transference.setAmount(transferenceOutDto.getAmount());
        transference.setDate(LocalDateTime.now());
        transference.setType("deposit");
        transference.setRecipient(account.getCvu());

        transferenceRepository.save(transference);

        account.setBalance(account.getBalance().add(transferenceOutDto.getAmount()));
        accountsRepository.save(account);

        Activity activity = new Activity();
        activity.setAccountId(accountId);
        activity.setType("deposit");
        activity.setAmount(transferenceOutDto.getAmount());
        activity.setDescription("Depósito de " + transferenceOutDto.getAmount() + " usando tarjeta " + card.getNumber());
        activity.setDate(LocalDateTime.now());

        activityRepository.save(activity);
    }


    @Transactional
    public void makeTransferFromCash(Long accountId, TransferRequestOutDTO transferRequest)
            throws AccountNotFoundException, BadRequestException {

        Account senderAccount = accountsRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Cuenta inexistente"));

        if (senderAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new InsufficientFundsException("Fondos insuficientes");
        }

        Account recipientAccount = findRecipientAccount(transferRequest.getRecipient());

        if (recipientAccount.getId().equals(senderAccount.getId())) {
            throw new BadRequestException("No puedes transferirte dinero a tu propia cuenta.");
        }

        senderAccount.setBalance(senderAccount.getBalance().subtract(transferRequest.getAmount()));
        recipientAccount.setBalance(recipientAccount.getBalance().add(transferRequest.getAmount()));

        accountsRepository.save(senderAccount);
        accountsRepository.save(recipientAccount);

        Transference transfer = new Transference();
        transfer.setAccountId(accountId);
        transfer.setAmount(transferRequest.getAmount());
        transfer.setType("transfer-out");
        transfer.setRecipient(transferRequest.getRecipient());
        transferenceRepository.save(transfer);

        Activity senderActivity = new Activity();
        senderActivity.setAccountId(accountId);
        senderActivity.setType("transfer-out");
        senderActivity.setAmount(transferRequest.getAmount().negate());
        senderActivity.setDescription(transferRequest.getRecipient());
        senderActivity.setDate(LocalDateTime.now());
        activityRepository.save(senderActivity);

        Activity recipientActivity = new Activity();
        recipientActivity.setAccountId(recipientAccount.getId());
        recipientActivity.setType("transfer-in");
        recipientActivity.setAmount(transferRequest.getAmount());
        recipientActivity.setDescription(senderAccount.getCvu());
        recipientActivity.setDate(LocalDateTime.now());
        activityRepository.save(recipientActivity);
    }

    // Método para encontrar la cuenta del destinatario
    public Account findRecipientAccount(String recipientIdentifier) throws AccountNotFoundException {
        // Intenta encontrar la cuenta por alias
        Account recipientAccount = accountsRepository.findByAlias(recipientIdentifier);
        if (recipientAccount != null) {
            return recipientAccount;
        }

        // Si no se encuentra por alias, intenta encontrar por CVU
        recipientAccount = accountsRepository.findByCvu(recipientIdentifier);
        if (recipientAccount != null) {
            return recipientAccount;
        }

        // Si no se encuentra por alias ni por CVU, lanzar excepción
        throw new AccountNotFoundException("Cuenta destinataria inexistente");
    }

    // Método para obtener las últimas 5 cuentas a las que se transfirió dinero
    public List<Transference> getLastTransferredAccounts(Long accountId) {
        return transferenceRepository.findTop5ByAccountIdOrderByDateDesc(accountId);
    }


}



