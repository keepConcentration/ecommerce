package com.phm.ecommerce.infrastructure.dlq;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@ConfigurationProperties(prefix = "dlq")
public class DLQProperties {

  private final int maxRetryCount;
  private final int retryIntervalMinutes;
  private final int backoffMultiplier;

  public DLQProperties(
      @DefaultValue("5") int maxRetryCount,
      @DefaultValue("1") int retryIntervalMinutes,
      @DefaultValue("2") int backoffMultiplier) {
    this.maxRetryCount = maxRetryCount;
    this.retryIntervalMinutes = retryIntervalMinutes;
    this.backoffMultiplier = backoffMultiplier;
  }
}
