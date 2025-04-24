package com.DigitalMoneyHouse.accountsservice.service.impl;

import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Card;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardAlreadyExistsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardNotFoundException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.CardRepository;
import com.DigitalMoneyHouse.accountsservice.service.ICardService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardServiceImpl implements ICardService {
    private final Logger LOGGER = LoggerFactory.getLogger(CardServiceImpl.class);

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<CardOutDTO> getCardsByAccountId(Long accountId) {
        List<Card> cards = cardRepository.findByAccountId(accountId);
        return cards.stream()
                .map(card -> modelMapper.map(card, CardOutDTO.class))
                .collect(Collectors.toList());
    }

    public CardOutDTO getCardById(Long accountId, Long cardId) throws ResourceNotFoundException {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        if (!card.getAccountId().equals(accountId)) {
            throw new AccessDeniedException("You do not have access to this card.");
        }

        return modelMapper.map(card, CardOutDTO.class);
    }

    @Override
    public CardOutDTO createCard(Long accountId, CreateCardEntryDTO createCardEntryDTO, String jwtToken) throws CardAlreadyExistsException, ResourceNotFoundException {
        return null;
    }

    public CardOutDTO createCard(Long accountId, CreateCardEntryDTO createCardEntryDTO)
            throws CardAlreadyExistsException, ResourceNotFoundException {

        String email = extractEmailFromSecurityContext();
        if (email == null) {
            throw new ResourceNotFoundException("No se pudo obtener el email del contexto de seguridad.");
        }
        LOGGER.info("Usuario autenticado: {}", email);

        Account account = accountsRepository.findByEmail(email);
        if (account == null) {
            throw new ResourceNotFoundException("No se encontró ninguna cuenta asociada al email.");
        }

        if (!account.getId().equals(accountId)) {
            throw new AccessDeniedException("No tienes permiso para agregar una tarjeta a esta cuenta.");
        }

        Optional<Card> existingCard = cardRepository.findByNumber(createCardEntryDTO.getNumber());
        if (existingCard.isPresent() && !existingCard.get().getAccountId().equals(accountId)) {
            throw new CardAlreadyExistsException("La tarjeta ya está asociada a otra cuenta.");
        }

        Card card = new Card();
        card.setAccountId(accountId);
        card.setNumber(createCardEntryDTO.getNumber());
        card.setName(createCardEntryDTO.getName());
        card.setExpiry(createCardEntryDTO.getExpiry());
        card.setCvc(createCardEntryDTO.getCvc());

        Card savedCard = cardRepository.save(card);
        return modelMapper.map(savedCard, CardOutDTO.class);
    }

    public void deleteCard(Long accountId, Long cardId) {
        Optional<Card> cardOptional = cardRepository.findById(cardId);
        if (cardOptional.isEmpty() || !cardOptional.get().getAccountId().equals(accountId)) {
            throw new CardNotFoundException("La tarjeta no se encontró o no está asociada a esta cuenta.");
        }
        cardRepository.delete(cardOptional.get());
    }

    private String extractEmailFromSecurityContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaim("email");
        }
        return null;
    }
    // Método alternativo solo para pruebas unitarias (sin usar SecurityContextHolder)
    public CardOutDTO createCardWithEmail(Long accountId, CreateCardEntryDTO createCardEntryDTO, String email)
            throws CardAlreadyExistsException, ResourceNotFoundException {

        if (email == null) {
            throw new ResourceNotFoundException("No se pudo obtener el email del token.");
        }

        Account account = accountsRepository.findByEmail(email);
        if (account == null) {
            throw new ResourceNotFoundException("No se encontró ninguna cuenta asociada al email.");
        }

        if (!account.getId().equals(accountId)) {
            throw new AccessDeniedException("No tienes permiso para agregar una tarjeta a esta cuenta.");
        }

        Optional<Card> existingCard = cardRepository.findByNumber(createCardEntryDTO.getNumber());
        if (existingCard.isPresent() && !existingCard.get().getAccountId().equals(accountId)) {
            throw new CardAlreadyExistsException("La tarjeta ya está asociada a otra cuenta.");
        }

        Card card = new Card();
        card.setAccountId(accountId);
        card.setNumber(createCardEntryDTO.getNumber());
        card.setName(createCardEntryDTO.getName());
        card.setExpiry(createCardEntryDTO.getExpiry());
        card.setCvc(createCardEntryDTO.getCvc());

        Card savedCard = cardRepository.save(card);
        return modelMapper.map(savedCard, CardOutDTO.class);
    }
    public CardServiceImpl(CardRepository cardRepository, AccountsRepository accountsRepository, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.accountsRepository = accountsRepository;
        this.modelMapper = modelMapper;
    }


}
