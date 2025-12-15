package com.phm.ecommerce.support;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class TestContainerSupport {

  // MySQL Configuration
  private static final String MYSQL_IMAGE = "mysql:8.0";
  private static final String MYSQL_DATABASE_NAME = "testdb";
  private static final String MYSQL_USERNAME = "test";
  private static final String MYSQL_PASSWORD = "test";
  private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

  // Redis Configuration
  private static final String REDIS_IMAGE = "redis:7.0-alpine";
  private static final int REDIS_PORT = 6379;

  private static final MySQLContainer<?> MYSQL_CONTAINER;
  private static final RedisContainer REDIS_CONTAINER;

  static {
    MYSQL_CONTAINER = new MySQLContainer<>(MYSQL_IMAGE)
        .withDatabaseName(MYSQL_DATABASE_NAME)
        .withUsername(MYSQL_USERNAME)
        .withPassword(MYSQL_PASSWORD);
    MYSQL_CONTAINER.start();

    REDIS_CONTAINER = new RedisContainer(DockerImageName.parse(REDIS_IMAGE));
    REDIS_CONTAINER.start();
  }

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    // MySQL properties
    registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> MYSQL_DRIVER_CLASS_NAME);

    // Redis properties (for Spring Data Redis)
    registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
    registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());

    // Redisson properties
    registry.add("redisson.host", REDIS_CONTAINER::getHost);
    registry.add("redisson.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());
  }
}
