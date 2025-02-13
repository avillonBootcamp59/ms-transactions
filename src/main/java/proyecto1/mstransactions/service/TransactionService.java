package proyecto1.mstransactions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import proyecto1.mstransactions.client.AccountClient;
import proyecto1.mstransactions.client.CreditClient;
import proyecto1.mstransactions.client.CustomerClient;
import proyecto1.mstransactions.dto.AccountDTO;
import proyecto1.mstransactions.dto.CreditDTO;
import proyecto1.mstransactions.entity.Transaction;
import proyecto1.mstransactions.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CreditClient creditClient;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;

    public Mono<Transaction> validateAndCreateTransaction(Transaction transaction) {
        if (transaction.getType().equalsIgnoreCase("PAGO_CREDITO")) {
            return validateCredit(transaction.getCreditId(), transaction.getAmount())
                    .flatMap(credit -> transactionRepository.save(transaction));
        } else {
            return validateAccount(transaction.getAccountId(), transaction.getAmount(), transaction.getType())
                    .flatMap(account -> transactionRepository.save(transaction));
        }
    }

    private Mono<CreditDTO> validateCredit(String creditId, Double amount) {
        try {
            CreditDTO credit = creditClient.getCreditById(creditId);
            if (credit.getCurrentDebt() < amount) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago excede la deuda actual."));
            }
            return Mono.just(credit);
        } catch (Exception e) {
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error en ms-credits"));
        }
    }

    private Mono<AccountDTO> validateAccount(String accountId, Double amount, String transactionType) {
        try {
            AccountDTO account = accountClient.getAccountById(accountId);
            if (account.getType().equalsIgnoreCase("AHORRO")) {
                return validateSavingAccountTransactions(account)
                        .flatMap(valid -> valid ? Mono.just(account) : Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Límite de movimientos alcanzado.")));
            }

            if (account.getType().equalsIgnoreCase("PLAZO_FIJO") && !isValidFixedDepositDate()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Depósitos o retiros solo permitidos en la fecha establecida."));
            }

            if (transactionType.equalsIgnoreCase("RETIRO") && account.getBalance() < amount) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fondos insuficientes."));
            }

            return Mono.just(account);
        } catch (Exception e) {
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error en ms-accounts"));
        }
    }

    private Mono<Boolean> validateSavingAccountTransactions(AccountDTO account) {
        return transactionRepository.findByAccountId(account.getId())
                .filter(transaction -> transaction.getDate().getMonth().equals(LocalDate.now().getMonth()))
                .count()
                .map(count -> count < account.getTransactionLimit());
    }

    private boolean isValidFixedDepositDate() {
        LocalDate today = LocalDate.now();
        LocalDate fixedDate = today.with(TemporalAdjusters.firstDayOfMonth()).plusDays(14); // Retiro o depósito día 15 del mes
        return today.equals(fixedDate);
    }

    public Flux<Transaction> getTransactionsByProduct(String productId) {
        return transactionRepository.findByAccountId(productId)
                .switchIfEmpty(transactionRepository.findByCreditId(productId));
    }
}