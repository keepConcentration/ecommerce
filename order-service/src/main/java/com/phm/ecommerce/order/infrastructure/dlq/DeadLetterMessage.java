package com.phm.ecommerce.order.infrastructure.dlq;

import java.time.LocalDateTime;

public record DeadLetterMessage(
    String id,
    String originalMessage,
    String exceptionClass,
    String errorMessage,
    int retryCount,
    LocalDateTime nextRetryAt,
    LocalDateTime failedAt
) {

  public static DeadLetterMessage create(
      String id,
      String originalMessage,
      String exceptionClass,
      String errorMessage) {
    return new DeadLetterMessage(
        id,
        originalMessage,
        exceptionClass,
        errorMessage,
        0,
        null,
        LocalDateTime.now()
    );
  }

  public DeadLetterMessage incrementRetryCount() {
    return new DeadLetterMessage(
        id,
        originalMessage,
        exceptionClass,
        errorMessage,
        retryCount + 1,
        nextRetryAt,
        failedAt
    );
  }

  public DeadLetterMessage withNextRetryAt(LocalDateTime nextRetryAt) {
    return new DeadLetterMessage(
        id,
        originalMessage,
        exceptionClass,
        errorMessage,
        retryCount,
        nextRetryAt,
        failedAt
    );
  }
}
