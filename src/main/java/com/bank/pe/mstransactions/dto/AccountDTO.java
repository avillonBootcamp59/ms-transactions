package com.bank.pe.mstransactions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {
    private String id;
    private String number;
    private String type;
    private String customerId;
    private Double balance;
    private int transactionLimit;
    private Double commissionFee;
}
