package proyecto1.mstransactions.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import proyecto1.mstransactions.dto.CreditDTO;

@FeignClient(name = "ms-credits", url = "http://localhost:8083/v1.0/credits")
public interface CreditClient {
    @GetMapping("/{id}")
    default CreditDTO getById(@PathVariable("id") String creditId) {
        try {
            return getCreditById(creditId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Crédito no encontrado");
        }
    }

    @GetMapping("/{id}")
    CreditDTO getCreditById(@PathVariable("id") String creditId);
}
