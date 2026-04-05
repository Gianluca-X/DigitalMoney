package com.DigitalMoneyHouse.accountsservice.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferenceServiceImpl implements ITransferenceService {

    private final TransferenceRepository transferenceRepository;
    private final AccountsRepository accountsRepository;
    private final CardRepository cardRepository;
    private final ActivityRepository activityRepository;

    public TransferenceServiceImpl(TransferenceRepository transferenceRepository,
                                   AccountsRepository accountsRepository,
                                   CardRepository cardRepository,
                                   ActivityRepository activityRepository) {
        this.transferenceRepository = transferenceRepository;
        this.accountsRepository = accountsRepository;
        this.cardRepository = cardRepository;
        this.activityRepository = activityRepository;
    }

    // 💳 DEPÓSITO CON TARJETA
    @Transactional
    public void registerTransferenceFromCards(TransferenceOutDTO dto)
            throws ResourceNotFoundException, UnauthorizedException {

        Account account = getAuthenticatedAccount();

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto debe ser mayor a 0");
        }

        Card card = cardRepository.findById(dto.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (!account.getId().equals(card.getAccountId())) {
            throw new UnauthorizedException("No puedes usar una tarjeta que no corresponde a tu cuenta.");
        }

        Transference transference = new Transference();
        transference.setAccountId(account.getId());
        transference.setCardId(card.getId());
        transference.setAmount(dto.getAmount());
        transference.setDate(LocalDateTime.now());
        transference.setType("deposit");
        transference.setRecipient(account.getCvu());
        account.setBalance(account.getBalance().add(dto.getAmount()));
        accountsRepository.save(account);
        transference.setStatus("SUCCESS");
        transferenceRepository.save(transference);

   

        Activity activity = new Activity();
        activity.setAccountId(account.getId());
        activity.setType("deposit");
        activity.setAmount(dto.getAmount());
        activity.setDescription("Depósito con tarjeta");
        activity.setDate(LocalDateTime.now());
        
        activityRepository.save(activity);
    }

    // 💸 TRANSFERENCIA ENTRE CUENTAS
    @Transactional
    public Transference makeTransferFromCash(TransferRequestOutDTO request) {

Account sender = accountsRepository
        .findByIdForUpdate(getAuthenticatedAccount().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto debe ser mayor a 0");
        }
       if (request.getRecipient() == null || request.getRecipient().isBlank()) {
            throw new BadRequestException("Destinatario inválido");
        }

        Account recipient = findRecipientAccount(request.getRecipient());

        if (sender.getId().equals(recipient.getId())) {
            throw new BadRequestException("No podés transferirte a vos mismo");
        }

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Fondos insuficientes");
        }

        Transference tx = new Transference();
        tx.setAccountId(sender.getId());
        tx.setRecipient(request.getRecipient());
        tx.setAmount(request.getAmount());
        tx.setType("transfer");
        tx.setDate(LocalDateTime.now());
      try {

            sender.setBalance(sender.getBalance().subtract(request.getAmount()));
            recipient.setBalance(recipient.getBalance().add(request.getAmount()));

            accountsRepository.save(sender);
            accountsRepository.save(recipient);

            tx.setStatus("SUCCESS");

        } catch (Exception e) {

            tx.setStatus("FAILED");
            throw e;

        } finally {
            transferenceRepository.save(tx);
        }


       
        // 📊 Actividad emisor
        Activity senderActivity = new Activity();
        senderActivity.setAccountId(sender.getId());
        senderActivity.setType("transfer-out");
        senderActivity.setAmount(request.getAmount().negate());
        senderActivity.setDescription(request.getRecipient());
        senderActivity.setDate(LocalDateTime.now());

        activityRepository.save(senderActivity);

        // 📊 Actividad receptor
        Activity recipientActivity = new Activity();
        recipientActivity.setAccountId(recipient.getId());
        recipientActivity.setType("transfer-in");
        recipientActivity.setAmount(request.getAmount());
        recipientActivity.setDescription(sender.getCvu());
        recipientActivity.setDate(LocalDateTime.now());

        activityRepository.save(recipientActivity);

        return tx;
    }

    // 🔍 BUSCAR DESTINATARIO
    public Account findRecipientAccount(String recipientIdentifier)
            throws ResourceNotFoundException {

        Account recipient = accountsRepository.findByAlias(recipientIdentifier);

        if (recipient != null) return recipient;

        recipient = accountsRepository.findByCvu(recipientIdentifier);

        if (recipient != null) return recipient;

        throw new ResourceNotFoundException("Cuenta destinataria inexistente");
    }

    // 📜 ÚLTIMAS TRANSFERENCIAS
    public List<Transference> getLastTransferredAccounts(Long accountId) {
        return transferenceRepository.findTop5ByAccountIdOrderByDateDesc(accountId);
    }

    // 🔐 USUARIO AUTENTICADO
    public Account getAuthenticatedAccount() {
        return (Account) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}