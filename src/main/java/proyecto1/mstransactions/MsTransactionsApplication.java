package proyecto1.mstransactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "proyecto1.mstransactions.repository")
public class MsTransactionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsTransactionsApplication.class, args);
	}

}
