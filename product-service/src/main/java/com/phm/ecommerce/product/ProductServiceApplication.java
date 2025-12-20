package com.phm.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
	"com.phm.ecommerce.product",
	"com.phm.ecommerce.common.config",
	"com.phm.ecommerce.common.application.lock",
	"com.phm.ecommerce.common.infrastructure"
})
@EnableJpaRepositories(basePackages = {
	"com.phm.ecommerce.common.infrastructure",
	"com.phm.ecommerce.product.infrastructure.outbox"
})
@EntityScan(basePackages = {
	"com.phm.ecommerce.common.domain",
	"com.phm.ecommerce.product.infrastructure.outbox"
})
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
