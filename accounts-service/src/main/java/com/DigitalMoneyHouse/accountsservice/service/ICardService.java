package com.DigitalMoneyHouse.accountsservice.service;

import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardAlreadyExistsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;

import java.util.List;

public interface ICardService {
     List<CardOutDTO> getCardsByAccountId(Long accountId);
     CardOutDTO getCardById(Long accountId, Long cardId) throws ResourceNotFoundException;
     CardOutDTO createCard(Long accountId, CreateCardEntryDTO createCardEntryDTO, String jwtToken) throws CardAlreadyExistsException, ResourceNotFoundException;
     void deleteCard(Long accountId, Long cardId);
}
