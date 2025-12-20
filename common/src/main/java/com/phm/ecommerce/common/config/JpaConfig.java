package com.phm.ecommerce.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.phm.ecommerce.infrastructure")
@EntityScan(basePackages = "com.phm.ecommerce")
public class JpaConfig {
}
