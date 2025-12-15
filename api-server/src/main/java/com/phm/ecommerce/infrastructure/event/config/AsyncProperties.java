package com.phm.ecommerce.infrastructure.event.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@ConfigurationProperties(prefix = "async.executor")
public class AsyncProperties {

  private final int corePoolSize;
  private final int maxPoolSize;
  private final int queueCapacity;
  private final String threadNamePrefix;

  public AsyncProperties(
      @DefaultValue("5") int corePoolSize,
      @DefaultValue("10") int maxPoolSize,
      @DefaultValue("100") int queueCapacity,
      @DefaultValue("event-async-") String threadNamePrefix) {
    this.corePoolSize = corePoolSize;
    this.maxPoolSize = maxPoolSize;
    this.queueCapacity = queueCapacity;
    this.threadNamePrefix = threadNamePrefix;
  }
}
