package com.DigitalMoneyHouse.accountsservice.service.impl;


import com.DigitalMoneyHouse.accountsservice.dto.entry.AccountEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.AccountOutDTO;

import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.TransactionRepository;
import com.DigitalMoneyHouse.accountsservice.service.IAccountService;
import com.DigitalMoneyHouse.accountsservice.dto.AccountCreationRequest;
import com.DigitalMoneyHouse.accountsservice.dto.AccountResponse;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;
@Slf4j
@Service
public class AccountsServiceImpl implements IAccountService {
    private final ModelMapper modelMapper;
    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public AccountsServiceImpl(ModelMapper modelMapper, AccountsRepository accountsRepository, TransactionRepository transactionRepository) {
        this.modelMapper = modelMapper;
        this.accountsRepository=accountsRepository;
        this.transactionRepository= transactionRepository;
        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
    }

    public AccountResponse getAccountSummary(Long accountId) throws ResourceNotFoundException {
        // Obtener la cuenta por ID
        Account account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Crear respuesta con saldo disponible
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setBalance(account.getBalance());
        return response;
    }

    public List<Transaction> getLastTransactions(Long accountId) {
        // Obtener los últimos 5 movimientos
        return transactionRepository.findTop5ByAccountIdOrderByDateDesc(accountId);
    }
    public Account getAccountEntityById(Long id) throws ResourceNotFoundException {
        return accountsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }


    public AccountOutDTO getAccountById(Long accountId) throws ResourceNotFoundException {
        // Obtener la cuenta de la base de datos
        Account account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        // Convertir la entidad Account a AccountDTO
        return modelMapper.map(account, AccountOutDTO.class);
    }

    public AccountOutDTO updateAccount(Long id, AccountEntryDTO accountEntryDTO) throws ResourceNotFoundException {
        // Verificar si la cuenta existe
        Account existingAccount = accountsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        // Actualizar los campos de la cuenta existente
        modelMapper.map(accountEntryDTO, existingAccount);

        // Guardar la cuenta actualizada en la base de datos
        Account updatedAccount = accountsRepository.save(existingAccount);

        // Usar ModelMapper para convertir la entidad actualizada a AccountOutDTO
        AccountOutDTO accountOutDTO = modelMapper.map(updatedAccount, AccountOutDTO.class);

        return accountOutDTO; // Retornar el DTO
    }

    public  Account findByEmail(String email) {
        return accountsRepository.findByEmail(email);
    }

    public List<AccountOutDTO> getAccounts() {
        List<Account> accounts = accountsRepository.findAll(); // Obtener todas las cuentas
        return accounts.stream()
                .map(account -> modelMapper.map(account, AccountOutDTO.class))// Convertir cada Account a AccountDTO
                .collect(Collectors.toList());
    }
@Transactional
    public AccountResponse createAccount(AccountCreationRequest request) {
        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setEmail(request.getEmail());
        account.setAlias(request.getAlias());
        account.setCvu(request.getCvu());
        account.setBalance(request.getInitialBalance());

        // Guarda la cuenta en la base de datos
        account = accountsRepository.save(account);


    AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setBalance(account.getBalance());
    log.info("✅ Cuenta creada con id={} email={} alias={}", account.getId(), account.getEmail(), account.getAlias());

        return response;
    }
    public void deleteAccount(Long id) throws ResourceNotFoundException {
        if (!accountsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cuenta no encontrada");
        }
        accountsRepository.deleteById(id);
    }

}
