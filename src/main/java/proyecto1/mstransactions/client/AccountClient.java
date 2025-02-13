package proyecto1.mstransactions.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import proyecto1.mstransactions.dto.AccountDTO;

@FeignClient(name = "ms-accounts", url = "http://localhost:8082/v1.0/accounts")
public interface AccountClient {
    @GetMapping("/{accountId}")
    default AccountDTO getById(@PathVariable("accountId") String accountId) {
        try {
            return getAccountById(accountId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrado");
        }
    }

    @GetMapping("/{accountId}")
    AccountDTO getAccountById(@PathVariable("accountId") String accountId);
}