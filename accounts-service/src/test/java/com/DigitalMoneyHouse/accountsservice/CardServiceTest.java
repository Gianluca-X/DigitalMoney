package com.DigitalMoneyHouse.accountsservice;

import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Card;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardAlreadyExistsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.CardRepository;
import com.DigitalMoneyHouse.accountsservice.security.JwtAuthenticationFilter;
import com.DigitalMoneyHouse.accountsservice.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

    @InjectMocks
    private CardServiceImpl cardServiceImpl;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private ModelMapper modelMapper;

    private CreateCardEntryDTO createCardEntryDTO;
    private Account mockAccount;
    private Card mockCard;
    private CardOutDTO mockCardOutDTO;

    // Atributos para testGetCardById_Success
    private Card card;
    private CardOutDTO cardOutDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Datos comunes para los tests
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

        // Atributos para testGetCardById_Success
        card = new Card();
        card.setId(1L);
        card.setAccountId(2L);
        card.setNumber("1234-5678-9012-3456");
        card.setName("John Doe");
        card.setExpiry("12/24");
        card.setCvc("123");

        cardOutDTO = new CardOutDTO();
        cardOutDTO.setId(1L);
        cardOutDTO.setAccountId(2L);
        cardOutDTO.setNumber("1234-5678-9012-3456");
        cardOutDTO.setName("John Doe");
        cardOutDTO.setExpiry("12/24");
        cardOutDTO.setCvc("123");
    }

    @Test
    void testCreateCard_Success() throws Exception {
        // Configurar mocks
        String jwtToken = "mockJwtToken";
        String email = "test@example.com";

        when(jwtAuthenticationFilter.extractEmailFromToken(jwtToken)).thenReturn(email);
        when(accountsRepository.findByEmail(email)).thenReturn(mockAccount);
        when(cardRepository.findByNumber(createCardEntryDTO.getNumber())).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);
        when(modelMapper.map(mockCard, CardOutDTO.class)).thenReturn(mockCardOutDTO);

        // Ejecutar método
        CardOutDTO result = cardServiceImpl.createCard(1L, createCardEntryDTO, jwtToken);

        // Verificar resultados
        assertNotNull(result);
        assertEquals(mockCardOutDTO.getId(), result.getId());
        assertEquals(mockCardOutDTO.getNumber(), result.getNumber());
        assertEquals(mockCardOutDTO.getName(), result.getName());

        // Verificar interacciones con los mocks
        verify(jwtAuthenticationFilter, times(1)).extractEmailFromToken(jwtToken);
        verify(accountsRepository, times(1)).findByEmail(email);
        verify(cardRepository, times(1)).findByNumber(createCardEntryDTO.getNumber());
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(modelMapper, times(1)).map(mockCard, CardOutDTO.class);
    }

    @Test
    void testCreateCard_EmailNotFound() {
        // Configurar mocks
        String jwtToken = "mockJwtToken";
        when(jwtAuthenticationFilter.extractEmailFromToken(jwtToken)).thenReturn(null);

        // Ejecutar y verificar excepción
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cardServiceImpl.createCard(1L, createCardEntryDTO, jwtToken);
        });

        assertEquals("No se pudo obtener el email del token.", exception.getMessage());

        // Verificar interacciones
        verify(jwtAuthenticationFilter, times(1)).extractEmailFromToken(jwtToken);
        verifyNoInteractions(accountsRepository, cardRepository);
    }

    @Test
    void testCreateCard_CardAlreadyExists() {
        // Configurar mocks
        String jwtToken = "mockJwtToken";
        String email = "test@example.com";
        when(jwtAuthenticationFilter.extractEmailFromToken(jwtToken)).thenReturn(email);
        when(accountsRepository.findByEmail(email)).thenReturn(mockAccount);
        when(cardRepository.findByNumber(createCardEntryDTO.getNumber())).thenReturn(Optional.of(mockCard));

        // Ejecutar y verificar excepción
        CardAlreadyExistsException exception = assertThrows(CardAlreadyExistsException.class, () -> {
            cardServiceImpl.createCard(1L, createCardEntryDTO, jwtToken);
        });

        assertEquals("La tarjeta ya está asociada a otra cuenta.", exception.getMessage());

        // Verificar interacciones
        verify(jwtAuthenticationFilter, times(1)).extractEmailFromToken(jwtToken);
        verify(accountsRepository, times(1)).findByEmail(email);
        verify(cardRepository, times(1)).findByNumber(createCardEntryDTO.getNumber());
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    void testGetCardById_Success() throws ResourceNotFoundException {
        // Configurar mocks
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(modelMapper.map(card, CardOutDTO.class)).thenReturn(cardOutDTO);

        // Ejecutar método
        CardOutDTO result = cardServiceImpl.getCardById(2L, 1L);

        // Verificar resultados
        assertNotNull(result);
        assertEquals(cardOutDTO.getId(), result.getId());
        assertEquals(cardOutDTO.getNumber(), result.getNumber());
        verify(cardRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(card, CardOutDTO.class);
    }
    @Test
    void testDeleteCard_Success() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setAccountId(accountId);

        // Configurar mocks
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(card); // Asegurar que el método delete no hace nada

        // Act
        cardServiceImpl.deleteCard(accountId, cardId);

        // Assert
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).delete(card);
    }
    @Test
    void testGetCardsByAccountId_Success() {
        // Arrange
        Long accountId = 1L;

        // Crear objetos Card
        Card card1 = new Card();
        card1.setId(1L);
        card1.setAccountId(accountId);
        card1.setNumber("1234-5678-9012-3456");
        card1.setName("John Doe");
        card1.setExpiry("12/24");
        card1.setCvc("123");

        Card card2 = new Card();
        card2.setId(2L);
        card2.setAccountId(accountId);
        card2.setNumber("2345-6789-0123-4567");
        card2.setName("Jane Doe");
        card2.setExpiry("01/25");
        card2.setCvc("456");

        List<Card> cards = Arrays.asList(card1, card2);

        // Crear objetos CardOutDTO esperados
        CardOutDTO cardOutDTO1 = new CardOutDTO(
                card1.getId(),
                card1.getNumber(),
                card1.getName(),
                card1.getExpiry(),
                card1.getAccountId(),
                card1.getCvc()
        );

        CardOutDTO cardOutDTO2 = new CardOutDTO(
                card2.getId(),
                card2.getNumber(),
                card2.getName(),
                card2.getExpiry(),
                card2.getAccountId(),
                card2.getCvc()
        );

        // Configurar mocks
        when(cardRepository.findByAccountId(accountId)).thenReturn(cards);
        when(modelMapper.map(card1, CardOutDTO.class)).thenReturn(cardOutDTO1);
        when(modelMapper.map(card2, CardOutDTO.class)).thenReturn(cardOutDTO2);

        // Act
        List<CardOutDTO> result = cardServiceImpl.getCardsByAccountId(accountId);

        // Assert
        assertNotNull(result, "La lista de resultados no debe ser null");
        assertEquals(2, result.size(), "La lista debe tener 2 elementos");

        assertNotNull(result.get(0), "El primer CardOutDTO no debe ser null");
        assertNotNull(result.get(1), "El segundo CardOutDTO no debe ser null");

        assertEquals(cardOutDTO1.getId(), result.get(0).getId());
        assertEquals(cardOutDTO2.getId(), result.get(1).getId());

        verify(cardRepository, times(1)).findByAccountId(accountId);
        verify(modelMapper, times(1)).map(card1, CardOutDTO.class);
        verify(modelMapper, times(1)).map(card2, CardOutDTO.class);
    }


}

