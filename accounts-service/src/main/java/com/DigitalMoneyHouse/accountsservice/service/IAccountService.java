package com.DigitalMoneyHouse.accountsservice.service;

import com.DigitalMoneyHouse.accountsservice.dto.AccountCreationRequest;
import com.DigitalMoneyHouse.accountsservice.dto.AccountResponse;
import com.DigitalMoneyHouse.accountsservice.dto.entry.AccountEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.AccountOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Transaction;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;

import java.util.List;

public interface IAccountService {
    AccountResponse getAccountSummary(Long accountId) throws ResourceNotFoundException;
     List<Transaction> getLastTransactions(Long accountId);
     AccountOutDTO getAccountById(Long accountId) throws ResourceNotFoundException;
     AccountOutDTO updateAccount(Long id, AccountEntryDTO accountEntryDTO) throws ResourceNotFoundException;
     Account findByEmail(String email);
     List<AccountOutDTO> getAccounts();
     AccountResponse createAccount(AccountCreationRequest request);

}
