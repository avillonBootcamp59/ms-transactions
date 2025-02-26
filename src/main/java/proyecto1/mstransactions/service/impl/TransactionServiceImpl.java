package proyecto1.mstransactions.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import proyecto1.mstransactions.client.AccountClient;
import proyecto1.mstransactions.client.CreditClient;
import proyecto1.mstransactions.dto.AccountDTO;
import proyecto1.mstransactions.dto.CreditDTO;
import proyecto1.mstransactions.entity.Transaction;
import proyecto1.mstransactions.repository.TransactionRepository;
import proyecto1.mstransactions.service.TransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CreditClient creditClient;
    private final AccountClient accountClient;

    private Mono<CreditDTO> validateCredit(String creditId, Double amount) {
        return creditClient.getCreditById(creditId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crédito no encontrado")))
                .flatMap(credit -> {
                    if (credit.getCurrentDebt() < amount) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "El pago excede la deuda actual."));
                    }
                    return Mono.just(credit);
                })
                .onErrorResume(ex -> {
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE, "Error en ms-credits"));
                });
    }

    private Mono<AccountDTO> validateAccount(String accountId, Double amount, String transactionType, Transaction transaction) {
        return accountClient.getAccountById(accountId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada")))
                .flatMap(account -> {
                    // 🔹 Validación de cuentas de ahorro
                    if ("AHORRO".equalsIgnoreCase(account.getType())) {
                        return validateSavingAccountTransactions(account)
                                .flatMap(valid -> valid ? validateAndCreateTransaction(transaction, account) :
                                        Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Límite de movimientos alcanzado.")));
                    }

                    // 🔹 Validación de cuentas a plazo fijo
                    if ("PLAZO_FIJO".equalsIgnoreCase(account.getType()) && !isValidFixedDepositDate()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Depósitos o retiros solo permitidos en la fecha establecida."));
                    }

                    // 🔹 Validación de saldo para retiros
                    if ("RETIRO".equalsIgnoreCase(transactionType) && account.getBalance() < amount) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fondos insuficientes."));
                    }

                    return validateAndCreateTransaction(transaction, account);
                })
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "Error en ms-accounts")));
    }


    private Mono<Boolean> validateSavingAccountTransactions(AccountDTO account) {
        return transactionRepository.findByAccountId(account.getId())
                .filter(transaction -> transaction.getTransactionDate().getMonth().equals(LocalDate.now().getMonth()))
                .count()
                .map(count -> count < account.getTransactionLimit());
    }

    private boolean isValidFixedDepositDate() {
        LocalDate today = LocalDate.now();
        LocalDate fixedDate = today.with(
                TemporalAdjusters.firstDayOfMonth()).plusDays(14); // Retiro o depósito día 15 del mes
        return today.equals(fixedDate);
    }

    @Override
    public Flux<Transaction> listTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Mono<Transaction> getTransaction(String id) {
        return transactionRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transacción no encontrado")));
    }

    public Flux<Transaction> getTransactionsByProduct(String productId) {
        return transactionRepository.findByAccountId(productId)
                .switchIfEmpty(transactionRepository.findByCreditId(productId));
    }

    @Override
    public Mono<Transaction> createTransaction(Transaction transaction) {
        if (transaction.getType().equalsIgnoreCase("PAGO_CREDITO")) {
            return validateCredit(transaction.getCreditId(), transaction.getAmount())
                    .flatMap(credit -> transactionRepository.save(transaction));
        } else {
            return validateAccount(transaction.getAccountId(), transaction.getAmount(), transaction.getType(), transaction)
                    .flatMap(account -> transactionRepository.save(transaction));
        }
    }


    public Mono<AccountDTO> validateAndCreateTransaction(Transaction transaction, AccountDTO account) {
        return transactionRepository.countByAccountIdAndTransactionDateBetween(
                transaction.getAccountId(),
                LocalDateTime.now().withDayOfMonth(1), // Primer día del mes
                LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth()) // Último día del mes
        ).flatMap(count -> {
            if (count >= account.getTransactionLimit()) {
                // Si se supera el límite, se aplica comisión
                transaction.setFee(account.getCommissionFee());
            } else {
                transaction.setFee(0.0); // Sin comisión
            }
            return transactionRepository.save(transaction).thenReturn(account);
        });
    }

    @Override
    public Mono<Long> countByAccountId(String accountId) {
        return transactionRepository.countByAccountId(accountId)
                .switchIfEmpty(Mono.just(0L));
    }

    @Override
    public Flux<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }

}