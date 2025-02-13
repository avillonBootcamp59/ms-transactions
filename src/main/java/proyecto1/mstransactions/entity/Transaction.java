package proyecto1.mstransactions.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String accountId;
    private String type;  // "deposito", "retiro", "pago"
    private double amount;
    private String creditId;
    private LocalDateTime date;

    public Transaction(){
        this.date = LocalDateTime.now();
    }
}
