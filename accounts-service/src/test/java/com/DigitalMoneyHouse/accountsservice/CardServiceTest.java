package com.DigitalMoneyHouse.accountsservice;

import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Card;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardAlreadyExistsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.CardRepository;
import com.DigitalMoneyHouse.accountsservice.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    private CardServiceImpl cardServiceImpl;

    @Mock private AccountsRepository accountsRepository;
    @Mock private ModelMapper modelMapper;
@Mock private  CardRepository cardRepository;
    private CreateCardEntryDTO createCardEntryDTO;
    private Account mockAccount;
    private Card mockCard;
    private CardOutDTO mockCardOutDTO;

    @BeforeEach
    void setUp() {
        cardServiceImpl = new CardServiceImpl(cardRepository, accountsRepository, modelMapper);

        createCardEntryDTO = CreateCardEntryDTO.builder()
                .accountId(1L)
                .number("1234567890123456")
                .name("Visa")
                .expiry("12/25")
                .cvc("123")
                .build();

        mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setEmail("test@example.com");

        mockCard = new Card();
        mockCard.setId(1L);
        mockCard.setAccountId(1L);
        mockCard.setNumber("1234567890123456");
        mockCard.setName("Visa");
        mockCard.setExpiry("12/25");
        mockCard.setCvc("123");

        mockCardOutDTO = CardOutDTO.builder()
                .id(1L)
                .accountId(1L)
                .number("1234567890123456")
                .name("Visa")
                .expiry("12/25")
                .cvc("123")
                .build();
    }

    @Test
    void testCreateCard_Success() throws Exception {
        String email = "test@example.com";

        when(accountsRepository.findByEmail(email)).thenReturn(mockAccount);
        when(cardRepository.findByNumber(createCardEntryDTO.getNumber())).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);
        when(modelMapper.map(mockCard, CardOutDTO.class)).thenReturn(mockCardOutDTO);

        CardOutDTO result = cardServiceImpl.createCardWithEmail(1L, createCardEntryDTO, email);

        assertNotNull(result);
        assertEquals(mockCardOutDTO.getId(), result.getId());
        assertEquals(mockCardOutDTO.getNumber(), result.getNumber());

        verify(accountsRepository).findByEmail(email);
        verify(cardRepository).findByNumber(createCardEntryDTO.getNumber());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void testCreateCard_EmailNotFound() {
        String email = null;

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cardServiceImpl.createCardWithEmail(1L, createCardEntryDTO, email);
        });

        assertEquals("No se pudo obtener el email del token.", exception.getMessage());
    }

    @Test
    void testCreateCard_CardAlreadyExists() {
        String email = "test@example.com";

        when(accountsRepository.findByEmail(email)).thenReturn(mockAccount);
        when(cardRepository.findByNumber(createCardEntryDTO.getNumber())).thenReturn(Optional.of(mockCard));

        CardAlreadyExistsException exception = assertThrows(CardAlreadyExistsException.class, () -> {
            cardServiceImpl.createCardWithEmail(1L, createCardEntryDTO, email);
        });

        assertEquals("La tarjeta ya está asociada a otra cuenta.", exception.getMessage());
    }

    @Test
    void testGetCardById_Success() throws ResourceNotFoundException {
        Card card = new Card();
        card.setId(1L);
        card.setAccountId(2L);
        card.setNumber("1234-5678-9012-3456");
        card.setName("John Doe");
        card.setExpiry("12/24");
        card.setCvc("123");

        CardOutDTO cardOutDTO = new CardOutDTO(1L, "1234-5678-9012-3456", "John Doe", "12/24", 2L, "123");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(modelMapper.map(card, CardOutDTO.class)).thenReturn(cardOutDTO);

        CardOutDTO result = cardServiceImpl.getCardById(2L, 1L);

        assertNotNull(result);
        assertEquals(cardOutDTO.getId(), result.getId());
    }

    @Test
    void testDeleteCard_Success() {
        Long accountId = 1L;
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setAccountId(accountId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(card);

        cardServiceImpl.deleteCard(accountId, cardId);

        verify(cardRepository).delete(card);
    }

    @Test
    void testGetCardsByAccountId_Success() {
        Long accountId = 1L;
        Card card1 = new Card(1L, "1111", "Visa", "12/24", accountId, "123");
        Card card2 = new Card(2L, "2222", "Master", "01/25", accountId, "456");

        List<Card> cards = Arrays.asList(card1, card2);
        CardOutDTO dto1 = new CardOutDTO(1L, "1111", "Visa", "12/24", accountId, "123");
        CardOutDTO dto2 = new CardOutDTO(2L, "2222", "Master", "01/25", accountId, "456");

        when(cardRepository.findByAccountId(accountId)).thenReturn(cards);
        when(modelMapper.map(card1, CardOutDTO.class)).thenReturn(dto1);
        when(modelMapper.map(card2, CardOutDTO.class)).thenReturn(dto2);

        List<CardOutDTO> result = cardServiceImpl.getCardsByAccountId(accountId);

        assertEquals(2, result.size());
    }
}
