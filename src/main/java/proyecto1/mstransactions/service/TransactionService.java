package proyecto1.mstransactions.service;

import proyecto1.mstransactions.entity.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    public Flux<Transaction> listTransactions();
    public Mono<Transaction> getTransaction(String id);
    public Flux<Transaction> getTransactionsByProduct(String id);
    public Mono<Transaction> createTransaction(Transaction Transaction);
    public Mono<Long> countByAccountId(String accountId);

}