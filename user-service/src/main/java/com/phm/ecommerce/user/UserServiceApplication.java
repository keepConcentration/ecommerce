package com.phm.ecommerce.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
	"com.phm.ecommerce.user",
	"com.phm.ecommerce.common.config",
	"com.phm.ecommerce.common.application.lock",
	"com.phm.ecommerce.common.infrastructure"
})
@EnableJpaRepositories(basePackages = "com.phm.ecommerce.common.infrastructure")
@EntityScan(basePackages = "com.phm.ecommerce.common.domain")
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
