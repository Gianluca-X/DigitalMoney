package com.DigitalMoneyHouse.accountsservice.controller;

import com.DigitalMoneyHouse.accountsservice.dto.*;
import com.DigitalMoneyHouse.accountsservice.dto.entry.AccountEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.AccountOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Transaction;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.service.impl.AccountsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsController.class);

    private final AccountsServiceImpl accountsService;
    private final AccountsRepository accountsRepository;

    public AccountsController(AccountsServiceImpl accountsService, AccountsRepository accountsRepository) {
        this.accountsService = accountsService;
        this.accountsRepository = accountsRepository;
    }

    // 📌 Crear cuenta (con autenticación Keycloak)
    @Operation(summary = "Creación y registro de una nueva cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta creada correctamente",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody AccountCreationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal Jwt jwt) {

        String internalToken = "secret-internal-token-123"; // inyectalo con @Value en prod

        if (authHeader != null && authHeader.equals("Bearer " + internalToken)) {
            LOGGER.info("✅ Solicitud interna autorizada con token interno.");
            AccountResponse response = accountsService.createAccount(request);
            return ResponseEntity.ok(response);
            
        }

        if (jwt != null) {
            String email = jwt.getClaim("email");
            LOGGER.info("📥 Creando cuenta para usuario autenticado: {}", email);
            AccountResponse response = accountsService.createAccount(request);
            return ResponseEntity.ok(response);
        }

        LOGGER.warn("Intento de creación de cuenta sin autenticación válida.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 📌 Obtener balance de cuenta
    @GetMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> getAccountSummary(@PathVariable Long id) throws ResourceNotFoundException {
        AccountResponse accountResponse = accountsService.getAccountSummary(id);
        return ResponseEntity.ok(accountResponse);
    }

    // 📌 Obtener las últimas transacciones
    @Operation(summary = "Obtener las últimas transacciones")
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getLastTransactions(@PathVariable Long id) {
        List<Transaction> transactions = accountsService.getLastTransactions(id);
        return ResponseEntity.ok(transactions);
    }

    // 📌 Obtener todas las cuentas
    @Operation(summary = "Obtener todas las cuentas")
    @GetMapping
    public ResponseEntity<List<AccountOutDTO>> getAllAccounts() {
        List<AccountOutDTO> accounts = accountsService.getAccounts();
        return ResponseEntity.ok(accounts);
    }

    // 📌 Obtener una cuenta por ID
    @Operation(summary = "Obtener cuenta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountOutDTO> getAccount(@PathVariable Long id) throws ResourceNotFoundException {
        AccountOutDTO account = accountsService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    // 📌 Actualizar cuenta
    @Operation(summary = "Actualizar cuenta")
    @PatchMapping("/{id}")
    public ResponseEntity<AccountOutDTO> updateAccount(
            @PathVariable Long id,
            @RequestBody AccountEntryDTO accountEntryDTO) throws ResourceNotFoundException {

        AccountOutDTO updatedAccount = accountsService.updateAccount(id, accountEntryDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    // 📌 Actualizar alias de cuenta
    @PatchMapping("/update/alias/{id}")
    public ResponseEntity<Map<String, String>> updateAlias(
            @PathVariable Long id,
            @RequestBody AccountUpdateRequest request) {

        try {
            accountsRepository.updateAlias(id, request.getAlias());
            LOGGER.info("✅ Alias actualizado correctamente para la cuenta ID: {}", id);
            return ResponseEntity.ok(Map.of("message", "Alias actualizado exitosamente"));
        } catch (Exception e) {
            LOGGER.error("❌ Error al actualizar el alias: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el alias en el servicio de usuarios."));
        }
    }
}
