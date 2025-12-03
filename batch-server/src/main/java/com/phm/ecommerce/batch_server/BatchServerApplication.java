package com.phm.ecommerce.batch_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.phm.ecommerce.infrastructure.repository")
@EntityScan(basePackages = "com.phm.ecommerce.domain")
@SpringBootApplication(scanBasePackages = "com.phm.ecommerce")
public class BatchServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchServerApplication.class, args);
	}

}
