package proyecto1.mstransactions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDTO {
    private String id;
    private String name;
    private String type; // Personal o Empresarial
    private String numberDocument; // DNI o RUC
    private String email;
}
