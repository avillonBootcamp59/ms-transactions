package proyecto1.mstransactions.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import proyecto1.mstransactions.dto.AccountDTO;
import proyecto1.mstransactions.dto.CreditDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class  AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }
    public Mono<AccountDTO> getAccountById(String id) {
        return webClient.get()
                .uri("/v1.0/accounts/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Cuenta no encontrado")))
                .onStatus(HttpStatus::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Error en el servicio ms-customer")))
                .bodyToMono(AccountDTO.class);
    }
}