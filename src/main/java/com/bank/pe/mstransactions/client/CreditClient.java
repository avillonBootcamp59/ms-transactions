package com.bank.pe.mstransactions.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import com.bank.pe.mstransactions.dto.CreditDTO;
import reactor.core.publisher.Mono;

@Component
public class CreditClient {

    private final WebClient webClient;

    public CreditClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8083").build();
    }

    public Mono<CreditDTO> getCreditById(String customerId) {
        return webClient.get()
                .uri("/v1.0/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Cliente no encontrado")))
                .onStatus(HttpStatus::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Error en el servicio ms-customer")))
                .bodyToMono(CreditDTO.class);
    }
}
