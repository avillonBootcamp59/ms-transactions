package com.bank.pe.mstransactions.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private String id;
    private String accountId;
    private String type;
    private double amount;
    private String creditId;
    private Double fee;
    private LocalDateTime transactionDate;
}
