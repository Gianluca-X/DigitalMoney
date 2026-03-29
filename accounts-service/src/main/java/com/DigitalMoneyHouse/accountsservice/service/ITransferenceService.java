package com.DigitalMoneyHouse.accountsservice.service;

import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferRequestOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferenceOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Transference;
import com.DigitalMoneyHouse.accountsservice.exceptions.BadRequestException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.exceptions.UnauthorizedException;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

public interface ITransferenceService {
     void registerTransferenceFromCards( TransferenceOutDTO transferenceOutDto) throws ResourceNotFoundException, UnauthorizedException;
     Transference makeTransferFromCash(TransferRequestOutDTO request) throws BadRequestException;
    Account findRecipientAccount(String recipientIdentifier) throws AccountNotFoundException;
     List<Transference> getLastTransferredAccounts(Long accountId);
}
