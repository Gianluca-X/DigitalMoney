package com.DigitalMoneyHouse.accountsservice.controller;

import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferRequestOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.TransferenceOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Transference;
import com.DigitalMoneyHouse.accountsservice.exceptions.InsufficientFundsException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.exceptions.UnauthorizedException;
import com.DigitalMoneyHouse.accountsservice.service.impl.TransferenceServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransferenceController {

    @Autowired
    private TransferenceServiceImpl transferenceServiceImpl;

    // 🔐 Obtener usuario autenticado


    // 💳 Depósito con tarjeta
    @Operation(summary = "Depósito con tarjeta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ingreso registrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Cuenta o tarjeta no encontrada")
    })
 @PostMapping("/cards")
public ResponseEntity<?> registerTransference(
        @RequestBody TransferenceOutDTO transferenceOutDto) {

    try {

        transferenceServiceImpl.registerTransferenceFromCards(transferenceOutDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Ingreso registrado con éxito");

    } catch (ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Cuenta o tarjeta no encontrada");

    } catch (UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Sin permisos");

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error al registrar el ingreso");
    }
}

    // 💸 Transferencia entre cuentas
    @Operation(summary = "Transferencia de dinero")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transferencia realizada"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "410", description = "Fondos insuficientes")
    })
    @PostMapping
    public ResponseEntity<?> makeTransfer(
            @RequestBody TransferRequestOutDTO transferRequest) {

        try {
            Transference tx =
                    transferenceServiceImpl.makeTransferFromCash(transferRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(tx);

        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Fondos insuficientes");

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Permisos insuficientes");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en el procesamiento de la transferencia");
        }
    }

    // 📜 Últimas transferencias del usuario autenticado
    @Operation(summary = "Obtener últimas transferencias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferencias obtenidas"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/last")
    public ResponseEntity<List<Transference>> getLastTransferredAccounts() {

    Account account = transferenceServiceImpl.getAuthenticatedAccount();

        List<Transference> lastTransfers =
                transferenceServiceImpl.getLastTransferredAccounts(account.getId());

        return ResponseEntity.ok(lastTransfers);
    }
}