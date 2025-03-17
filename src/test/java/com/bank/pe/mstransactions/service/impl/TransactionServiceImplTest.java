package com.bank.pe.mstransactions.service.impl;

import com.bank.pe.mstransactions.client.AccountClient;
import com.bank.pe.mstransactions.client.CreditClient;
import com.bank.pe.mstransactions.dto.AccountDTO;
import com.bank.pe.mstransactions.dto.CreditDTO;
import com.bank.pe.mstransactions.entity.Transaction;
import com.bank.pe.mstransactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CreditClient creditClient;

    @Mock
    private AccountClient accountClient;

    @Test
    public void testListTransactions() {
        Transaction transaction = new Transaction();
        when(transactionRepository.findAll()).thenReturn(Flux.just(transaction));

        Flux<Transaction> result = transactionService.listTransactions();

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }

    @Test
    public void testGetTransaction() {
        String id = "12345";
        Transaction transaction = new Transaction();
        when(transactionRepository.findById(id)).thenReturn(Mono.just(transaction));

        Mono<Transaction> result = transactionService.getTransaction(id);

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }

    @Test
    public void testGetTransaction_NotFound() {
        String id = "12345";
        when(transactionRepository.findById(id)).thenReturn(Mono.empty());

        Mono<Transaction> result = transactionService.getTransaction(id);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    public void testGetTransactionsByProduct() {
        String productId = "12345";
        Transaction transaction = new Transaction();

        when(transactionRepository.findByAccountId(productId)).thenReturn(Flux.just(transaction));
        when(transactionRepository.findByCreditId(productId)).thenReturn(Flux.empty());

        Flux<Transaction> result = transactionService.getTransactionsByProduct(productId);

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }

    @Test
    public void testCreateTransaction_PagoCredito() {
        Transaction transaction = new Transaction();
        transaction.setType("PAGO_CREDITO");
        transaction.setCreditId("credit123");
        transaction.setAmount(100.0);
        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setCurrentDebt(200.0);

        when(creditClient.getCreditById(transaction.getCreditId())).thenReturn(Mono.just(creditDTO));
        when(transactionRepository.save(transaction)).thenReturn(Mono.just(transaction));

        Mono<Transaction> result = transactionService.createTransaction(transaction);

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }

    @Test
    public void testCountByAccountId() {
        String accountId = "12345";
        Long count = 10L;

        when(transactionRepository.countByAccountId(accountId)).thenReturn(Mono.just(count));

        Mono<Long> result = transactionService.countByAccountId(accountId);

        StepVerifier.create(result)
                .expectNext(count)
                .verifyComplete();
    }

    @Test
    public void testGetTransactionsByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Transaction transaction = new Transaction();

        when(transactionRepository.findByTransactionDateBetween(startDate, endDate)).thenReturn(Flux.just(transaction));

        Flux<Transaction> result = transactionService.getTransactionsByDateRange(startDate, endDate);

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();
    }
}