package com.bank.pe.mstransactions.controllers;

import com.bank.pe.mstransactions.dto.TransactionDTO;
import com.bank.pe.mstransactions.entity.Transaction;
import com.bank.pe.mstransactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Test
    public void testGetAllTransactions() {
        Transaction transaction = new Transaction();
        when(transactionService.listTransactions()).thenReturn(Flux.just(transaction));

        Flux<Transaction> result = transactionController.getAllTransactions();

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }

    @Test
    public void testCreateTransaction() {
        TransactionDTO transactionDTO = new TransactionDTO();
        Transaction transaction = new Transaction();
        transaction.setId("12345");

        doReturn(Mono.just(transaction)).when(transactionService).createTransaction(any(Transaction.class));
        Mono<ResponseEntity<Map<String, String>>> result = transactionController.createTransaction(transactionDTO);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CREATED &&
                        response.getBody().get("message").equals("Transacción creada exitosamente") &&
                        response.getBody().get("id").equals("12345"))
                .verifyComplete();
    }

    @Test
    public void testCreateTransaction_Error() {
        TransactionDTO transactionDTO = new TransactionDTO();
        Transaction transaction = new Transaction();
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la validación");

        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(Mono.error(exception));

        Mono<ResponseEntity<Map<String, String>>> result = transactionController.createTransaction(transactionDTO);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                        response.getBody().get("error").equals("Error en la validación") &&
                        response.getBody().get("status").equals("400"))
                .verifyComplete();
    }

    @Test
    public void testGetTransactionsByProduct() {
        String productId = "12345";
        Transaction transaction = new Transaction();

        when(transactionService.getTransactionsByProduct(productId)).thenReturn(Flux.just(transaction));

        Flux<Transaction> result = transactionController.getTransactionsByProduct(productId);

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }

    @Test
    public void testGetTransactionsByProduct_NotFound() {
        String productId = "12345";

        when(transactionService.getTransactionsByProduct(productId)).thenReturn(Flux.empty());

        Flux<Transaction> result = transactionController.getTransactionsByProduct(productId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    public void testGetTransactionCount() {
        String accountId = "12345";
        Long count = 10L;

        when(transactionService.countByAccountId(accountId)).thenReturn(Mono.just(count));

        Mono<Long> result = transactionController.getTransactionCount(accountId);

        StepVerifier.create(result)
                .expectNext(count)
                .verifyComplete();
    }

    @Test
    public void testGetTransactionsByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Transaction transaction = new Transaction();

        when(transactionService.getTransactionsByDateRange(startDate, endDate)).thenReturn(Flux.just(transaction));

        Flux<Transaction> result = transactionController.getTransactionsByDateRange(startDate, endDate);

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }
}