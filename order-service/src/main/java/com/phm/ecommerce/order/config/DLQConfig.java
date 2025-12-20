package com.phm.ecommerce.order.config;

import com.phm.ecommerce.order.infrastructure.dlq.DLQProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DLQProperties.class)
public class DLQConfig {
}
