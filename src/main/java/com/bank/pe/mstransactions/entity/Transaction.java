package com.bank.pe.mstransactions.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @BsonId
    private String id;
    private String accountId;
    private String type;  // "deposito", "retiro", "pago"
    private double amount;
    private String creditId;
    private Double fee; // comisión generada
    private LocalDateTime transactionDate;
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
    }

}
