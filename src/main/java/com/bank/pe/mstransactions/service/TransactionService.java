package com.bank.pe.mstransactions.service;

import com.bank.pe.mstransactions.entity.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionService {
    public Flux<Transaction> listTransactions();
    public Mono<Transaction> getTransaction(String id);
    public Flux<Transaction> getTransactionsByProduct(String id);
    public Mono<Transaction> createTransaction(Transaction Transaction);
    public Mono<Long> countByAccountId(String accountId);
    public Flux<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}