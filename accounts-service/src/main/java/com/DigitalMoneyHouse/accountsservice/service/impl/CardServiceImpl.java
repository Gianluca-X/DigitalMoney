package com.DigitalMoneyHouse.accountsservice.service.impl;

import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Card;
import com.DigitalMoneyHouse.accountsservice.exceptions.*;
import com.DigitalMoneyHouse.accountsservice.repository.CardRepository;
import com.DigitalMoneyHouse.accountsservice.service.ICardService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CardServiceImpl implements ICardService {

    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;

    public CardServiceImpl(CardRepository cardRepository,
                           ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.modelMapper = modelMapper;
    }

    private Account getAuthenticatedAccount() {
        return (Account) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void validateOwnership(Long accountId) throws UnauthorizedException {
        Account authenticatedAccount = getAuthenticatedAccount();

        if (!authenticatedAccount.getId().equals(accountId)) {
            throw new UnauthorizedException(
                    "No tienes permiso para operar sobre esta cuenta."
            );
        }
    }

    @Override
    public List<CardOutDTO> getCardsByAccountId(Long accountId) {
        validateOwnership(accountId);

        return cardRepository.findByAccountId(accountId)
                .stream()
                .map(card -> modelMapper.map(card, CardOutDTO.class))
                .toList();
    }

    @Override
    public CardOutDTO getCardById(Long accountId, Long cardId)
            throws ResourceNotFoundException {

        validateOwnership(accountId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarjeta no encontrada")
                );

        if (!card.getAccountId().equals(accountId)) {
            throw new UnauthorizedException(
                    "La tarjeta no pertenece a esta cuenta."
            );
        }

        return modelMapper.map(card, CardOutDTO.class);
    }

    @Override
    public CardOutDTO createCard(Long accountId,
                                 CreateCardEntryDTO dto)
            throws CardAlreadyExistsException,
                   BadRequestException,
                   UnauthorizedException {

        validateOwnership(accountId);

        if (dto.getNumber() == null || dto.getNumber().isBlank()) {
            throw new BadRequestException(
                    "El número de la tarjeta no puede estar vacío"
            );
        }

        if (cardRepository.findByNumber(dto.getNumber()).isPresent()) {
            throw new CardAlreadyExistsException(
                    "La tarjeta ya existe en el sistema."
            );
        }

        Card card = new Card();
        card.setAccountId(accountId);
        card.setNumber(dto.getNumber());
        card.setName(dto.getName());
        card.setExpiry(dto.getExpiry());
        card.setCvc(dto.getCvc());

        Card saved = cardRepository.save(card);

        return modelMapper.map(saved, CardOutDTO.class);
    }

    @Override
    public void deleteCard(Long accountId, Long cardId)
        throws ResourceNotFoundException, UnauthorizedException {

        validateOwnership(accountId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarjeta no encontrada")
                );

        if (!card.getAccountId().equals(accountId)) {
            throw new UnauthorizedException(
                    "La tarjeta no pertenece a esta cuenta."
            );
        }

        cardRepository.delete(card);
    }
}