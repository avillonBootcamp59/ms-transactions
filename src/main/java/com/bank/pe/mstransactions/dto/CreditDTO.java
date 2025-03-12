package com.bank.pe.mstransactions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditDTO {
    private String id;
    private String customerId;
    private String creditType;
    private Double creditLimit;
    private Double currentDebt;
    private Double availableLimit;
}
