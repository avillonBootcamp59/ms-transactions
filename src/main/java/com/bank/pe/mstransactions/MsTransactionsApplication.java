package com.bank.pe.mstransactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "com.bank.pe.mstransactions.repository")
public class MsTransactionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsTransactionsApplication.class, args);
	}

}
