package proyecto1.mstransactions.rest;

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
import proyecto1.mstransactions.repository.TransactionRepository;
import proyecto1.mstransactions.service.TransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import proyecto1.mstransactions.entity.Transaction;

@RestController
@RequestMapping("/v1.0/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "Gestión de transacciones bancarias")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionRepository repository;
    private final TransactionService transactionService;

    @Operation(summary = "Obtener todas las transacciones", description = "Lista todas las transacciones realizadas por clientes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de transacciones obtenida correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public Flux<Transaction> getAllTransactions() {
        logger.info("Obteniendo todas las transacciones bancarias");
        return repository.findAll();
    }

    @Operation(summary = "Registrar una nueva transacción", description = "Permite registrar depósitos, retiros y pagos de créditos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transacción creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Error en la validación de la transacción"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public Mono<ResponseEntity<Transaction>> createTransaction(@RequestBody Transaction transaction) {
        return transactionService.validateAndCreateTransaction(transaction)
                .map(savedTransaction -> ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction))
                .onErrorResume(error -> {
                    logger.error("Error al crear Transacción: {}", error.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(null));
                });
    }

    @Operation(summary = "Obtener transacciones por ID de producto", description = "Lista las transacciones asociadas a una cuenta bancaria o un crédito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacciones obtenidas correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron transacciones para el producto especificado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{productId}")
    public Flux<Transaction> getTransactionsByProduct(@PathVariable String productId) {
        return transactionService.getTransactionsByProduct(productId)
                .switchIfEmpty(Flux.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "No se encontraron transacciones")));
    }
}
