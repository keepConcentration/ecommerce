package com.phm.ecommerce.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for enabling scheduled tasks (e.g., Outbox event publisher).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
