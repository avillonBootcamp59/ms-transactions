package com.bank.pe.mstransactions.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import com.bank.pe.mstransactions.dto.AccountDTO;
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