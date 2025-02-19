package proyecto1.mstransactions.entity;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "transactions")
public class Transaction {

    @BsonId
    private String id;
    private String accountId;
    private String type;  // "deposito", "retiro", "pago"
    private double amount;
    private String creditId;
    private LocalDateTime date;
    private Double fee; // comisión generada

    public Transaction() {
        this.date = LocalDateTime.now();
    }

}
