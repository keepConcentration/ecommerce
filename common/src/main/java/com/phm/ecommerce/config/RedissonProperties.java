package com.phm.ecommerce.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

  private final String host;
  private final int port;
  private final int connectionPoolSize;
  private final int connectionMinimumIdleSize;
  private final int idleConnectionTimeout;
  private final int connectTimeout;
  private final int timeout;

  public RedissonProperties(
      @DefaultValue("localhost") String host,
      @DefaultValue("6379") int port,
      @DefaultValue("50") int connectionPoolSize,
      @DefaultValue("10") int connectionMinimumIdleSize,
      @DefaultValue("10000") int idleConnectionTimeout,
      @DefaultValue("3000") int connectTimeout,
      @DefaultValue("3000") int timeout) {
    this.host = host;
    this.port = port;
    this.connectionPoolSize = connectionPoolSize;
    this.connectionMinimumIdleSize = connectionMinimumIdleSize;
    this.idleConnectionTimeout = idleConnectionTimeout;
    this.connectTimeout = connectTimeout;
    this.timeout = timeout;
  }
}
