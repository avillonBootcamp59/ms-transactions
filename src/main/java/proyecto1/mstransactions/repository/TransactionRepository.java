package proyecto1.mstransactions.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import proyecto1.mstransactions.entity.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByAccountId(String accountId);
    Flux<Transaction> findByCreditId(String creditId);
    Mono<Long> countByAccountId(String accountId);
    Mono<Long> countByAccountIdAndDateBetween(String accountId, LocalDateTime startDate, LocalDateTime endDate);
}

