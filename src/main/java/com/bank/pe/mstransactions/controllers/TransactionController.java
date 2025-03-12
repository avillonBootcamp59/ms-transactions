package com.bank.pe.mstransactions.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.bank.pe.mstransactions.service.TransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.bank.pe.mstransactions.entity.Transaction;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "Gestión de transacciones bancarias")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    @Operation(summary = "Obtener todas las transacciones",
               description = "Lista todas las transacciones realizadas por clientes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de transacciones obtenida correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public Flux<Transaction> getAllTransactions() {
        logger.info("Obteniendo todas las transacciones bancarias");
        return transactionService.listTransactions();
    }

    @Operation(summary = "Registrar una nueva transacción",
               description = "Permite registrar depósitos, retiros y pagos de créditos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transacción creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Error en la validación de la transacción"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>>  createTransaction(@RequestBody Transaction transaction) {
        return transactionService.createTransaction(transaction)
                .map(savedAccount -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Transacción creada exitosamente", "id", savedAccount.getId())))
                .onErrorResume(ResponseStatusException.class, ex -> {
                    Map<String, String> errorResponse = Map.of(
                            "error", ex.getReason(),
                            "status", String.valueOf(ex.getRawStatusCode())
                    );
                    return Mono.just(ResponseEntity.status(ex.getRawStatusCode()).body(errorResponse));
                });
    }

    @Operation(summary = "Obtener transacciones por ID de producto",
               description = "Lista las transacciones asociadas a una cuenta bancaria o un crédito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacciones obtenidas correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron transacciones para el producto"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{productId}")
    public Flux<Transaction> getTransactionsByProduct(@PathVariable String productId) {
        return transactionService.getTransactionsByProduct(productId)
                .switchIfEmpty(Flux.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "No se encontraron transacciones")));
    }
    @Operation(summary = "Contar transacciones de una cuenta")
    @ApiResponse(responseCode = "200", description = "Número de transacciones obtenidas correctamente")
    @GetMapping("/count/{accountId}")
    public Mono<Long> getTransactionCount(@PathVariable String accountId) {
        return transactionService.countByAccountId(accountId);
    }
    @Operation(summary = "Reporte de transacción",
            description = "Permite visualizar depósitos, retiros y pagos de créditos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Obtención exitosa"),
            @ApiResponse(responseCode = "400", description = "Error en la validación de la transacción"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/report")
    public Flux<Transaction> getTransactionsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return transactionService.getTransactionsByDateRange(startDate, endDate);
    }

}
