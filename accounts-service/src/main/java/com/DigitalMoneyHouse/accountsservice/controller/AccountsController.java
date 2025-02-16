package com.DigitalMoneyHouse.accountsservice.controller;


import com.DigitalMoneyHouse.accountsservice.dto.AccountCreationRequest;
import com.DigitalMoneyHouse.accountsservice.dto.AccountResponse;
import com.DigitalMoneyHouse.accountsservice.dto.AccountUpdateRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountsController {
    @Autowired
    private AccountsServiceImpl accountsServiceImpl;
    private final AccountsRepository accountsRepository;
    private final WebClient webClient;

    @Autowired
    public AccountsController(AccountsRepository accountsRepository, WebClient.Builder webClientBuilder) {
        this.accountsRepository = accountsRepository;
        this.webClient = webClientBuilder.baseUrl("http://gateway:8084").build();
    }
    @Operation(summary = "Creacion y registro de una nueva cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta creada correctamente",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreationRequest request) {
        AccountResponse response = accountsServiceImpl.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> getAccountSummary(@PathVariable Long id) throws ResourceNotFoundException {
        AccountResponse accountResponse = accountsServiceImpl.getAccountSummary(id);
        return ResponseEntity.ok(accountResponse);
    }
    @Operation(summary = "Obtener las ultimas transacciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transacciones obtenidas con exito",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transaction.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getLastTransactions(@PathVariable Long id) {
        List<Transaction> transactions = accountsServiceImpl.getLastTransactions(id);
        return ResponseEntity.ok(transactions);
    }
    @Operation(summary = "Obtener Todas las Cuentas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuentas Obtenidas con exito",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountOutDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })

    @GetMapping
    public ResponseEntity<List<AccountOutDTO>> getAllAccounts() {
        List<AccountOutDTO> accounts = accountsServiceImpl.getAccounts();
        return ResponseEntity.ok(accounts);
    }
    @Operation(summary = "Obtener Cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta obtenida con exito",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountOutDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })

    @GetMapping("/{id}")
    public ResponseEntity<AccountOutDTO> getAccount(@PathVariable Long id) throws ResourceNotFoundException {
        AccountOutDTO account = accountsServiceImpl.getAccountById(id);
        return ResponseEntity.ok(account);
    }
    @Operation(summary = "Actualizar Cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuentas actualizada con exito",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountOutDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AccountOutDTO> updateAccount(@PathVariable Long id, @RequestBody AccountEntryDTO accountEntryDTO) throws ResourceNotFoundException {
        AccountOutDTO updatedAccount = accountsServiceImpl.updateAccount(id, accountEntryDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    @PatchMapping("/update/alias/{id}")
    public Mono<ResponseEntity<Map<String, String>>> updateAlias(@PathVariable Long id, @RequestBody AccountUpdateRequest request) {
        accountsRepository.updateAlias(id, request.getAlias());
        return webClient.patch()
                .uri("http://localhost:8085/users/update/alias/{id}", id)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok(Collections.singletonMap("message", "Alias actualizado exitosamente")))
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(500).body(Collections.singletonMap("error", "Error al actualizar el alias en el servicio de usuarios: " + e.getMessage())));
                });
    }

}
