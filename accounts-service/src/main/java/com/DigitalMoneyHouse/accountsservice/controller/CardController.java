package com.DigitalMoneyHouse.accountsservice.controller;

import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.exceptions.BadRequestException;
import com.DigitalMoneyHouse.accountsservice.exceptions.CardAlreadyExistsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.exceptions.UnauthorizedException;
import com.DigitalMoneyHouse.accountsservice.service.ICardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts/{accountId}/cards")
public class CardController {

    private final ICardService cardService;

    public CardController(ICardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Obtener todas las tarjetas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarjetas obtenidas con éxito"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @GetMapping
    public ResponseEntity<List<CardOutDTO>> getAllCards(
            @PathVariable Long accountId)
            throws UnauthorizedException {

        return ResponseEntity.ok(
                cardService.getCardsByAccountId(accountId)
        );
    }

    @Operation(summary = "Obtener tarjeta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarjeta obtenida con éxito"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/{cardId}")
    public ResponseEntity<CardOutDTO> getCard(
            @PathVariable Long accountId,
            @PathVariable Long cardId)
            throws ResourceNotFoundException, UnauthorizedException {

        return ResponseEntity.ok(
                cardService.getCardById(accountId, cardId)
        );
    }

    @Operation(summary = "Crear tarjeta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarjeta creada con éxito"),
            @ApiResponse(responseCode = "409", description = "Tarjeta ya existe"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<CardOutDTO> createCard(
            @PathVariable Long accountId,
            @Valid @RequestBody CreateCardEntryDTO dto)
            throws CardAlreadyExistsException,
                   ResourceNotFoundException,
                   BadRequestException,
                   UnauthorizedException {

        CardOutDTO created = cardService.createCard(accountId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Eliminar tarjeta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tarjeta eliminada"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long accountId,
            @PathVariable Long cardId)
            throws ResourceNotFoundException, UnauthorizedException {

        cardService.deleteCard(accountId, cardId);
        return ResponseEntity.noContent().build();
    }
}