package com.DigitalMoneyHouse.accountsservice.service.impl;

import com.DigitalMoneyHouse.accountsservice.appConfig.ModelMapperConfig;
import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;

import com.DigitalMoneyHouse.accountsservice.entities.Card;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardAlreadyExistsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardNotFoundException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;

import com.DigitalMoneyHouse.accountsservice.repository.CardRepository;
import com.DigitalMoneyHouse.accountsservice.security.JwtAuthenticationFilter;

import com.DigitalMoneyHouse.accountsservice.service.ICardService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    AccountsRepository accountsRepository;

    @Autowired
    private ModelMapper modelMapper; // Inyectamos la instancia centralizada

    public CardServiceImpl(AccountsRepository accountsRepository,
                           JwtAuthenticationFilter jwtAuthenticationFilter,
                           CardRepository cardRepository,
                           ModelMapper modelMapper) { // Agregar al constructor
        this.accountsRepository = accountsRepository;
        this.cardRepository = cardRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.modelMapper = modelMapper; // Inicializar la instancia
    }

    public List<CardOutDTO> getCardsByAccountId(Long accountId) {
        List<Card> cards = cardRepository.findByAccountId(accountId);
        return cards.stream()
                .map(card -> modelMapper.map(card,CardOutDTO.class))
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

    // Método para crear y agregar una tarjeta
    public CardOutDTO createCard(Long accountId, CreateCardEntryDTO createCardEntryDTO, String jwtToken) throws CardAlreadyExistsException, ResourceNotFoundException {
        // Extraer el email del token
        String email = jwtAuthenticationFilter.extractEmailFromToken(jwtToken);
        if (email == null) {
            throw new ResourceNotFoundException("No se pudo obtener el email del token.");
        }
        LOGGER.info("JWT Token extraído: {}", email);

        // Buscar el accountId por email
        Account account = accountsRepository.findByEmail(email);
        if (account == null) {
            throw new ResourceNotFoundException("No se encontró ninguna cuenta asociada al email.");
        }
        LOGGER.info("Cuenta encontrada: {}", account);
        Long accountIdFromToken = account.getId();

        // Comparar el accountId del token con el accountId del path variable
        LOGGER.info("Comparando accountId del token: {} con accountId proporcionado: {}", accountIdFromToken, accountId);
        if (!accountIdFromToken.equals(accountId)) {
            throw new ResourceNotFoundException("No tienes permiso para agregar una tarjeta a esta cuenta.");
        }

        // Verificar si la tarjeta ya está asociada a otra cuenta
        Optional<Card> existingCard = cardRepository.findByNumber(createCardEntryDTO.getNumber());
        if (existingCard.isPresent()) {
            LOGGER.info("Tarjeta encontrada: {}", existingCard.get());
            LOGGER.info("Account ID de la tarjeta existente: {}", existingCard.get().getAccountId());
            LOGGER.info("Comparando con accountId proporcionado: {}", accountId);
            if (!existingCard.get().getAccountId().equals(accountId)) {
                throw new CardAlreadyExistsException("La tarjeta ya está asociada a otra cuenta.");
            }
        }

        // Crear nueva tarjeta
        Card card = new Card();
        card.setAccountId(accountId);
        card.setNumber(createCardEntryDTO.getNumber());
        card.setName(createCardEntryDTO.getName());
        card.setExpiry(createCardEntryDTO.getExpiry());
        card.setCvc(createCardEntryDTO.getCvc());

        Card savedCard = cardRepository.save(card);
        // Convertir la entidad guardada a CardOutDTO
        CardOutDTO cardOutDTO = modelMapper.map(savedCard, CardOutDTO.class);
        LOGGER.info("Tarjeta convertida a DTO: {}", cardOutDTO);

        // Retornar el DTO
        return cardOutDTO;
    }


    // Método para eliminar una tarjeta
    public void deleteCard(Long accountId, Long cardId) {
        // Verificar si la tarjeta existe y está asociada a la cuenta
        Optional<Card> cardOptional = cardRepository.findById(cardId);
        if (cardOptional.isEmpty() || !cardOptional.get().getAccountId().equals(accountId)) {
            throw new CardNotFoundException("La tarjeta no se encontró o no está asociada a esta cuenta.");
        }
        // Eliminar la tarjeta
        cardRepository.delete(cardOptional.get());

    }

}

