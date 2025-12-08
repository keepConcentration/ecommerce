package com.phm.ecommerce.infrastructure.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisCacheKeys {

  private static final String PRODUCT_PREFIX = "product:";
  private static final String COUPON_QUEUE_PREFIX = "coupon:queue:";
  private static final String COUPON_QUEUE_SEQUENCE_PREFIX = "coupon:queue:sequence:";
  private static final String COUPON_RETRY_QUEUE_PREFIX = "coupon:retry:queue:";
  private static final String COUPON_DLQ_PREFIX = "coupon:dlq:";

  public static final String PRODUCT_RANKING = "product:ranking:total";

  public static String productCache(Long productId) {
    return PRODUCT_PREFIX + productId;
  }

  public static String couponQueue(Long couponId) {
    return COUPON_QUEUE_PREFIX + couponId;
  }

  public static String couponQueueSequence(Long couponId) {
    return COUPON_QUEUE_SEQUENCE_PREFIX + couponId;
  }

  public static String couponRetryQueue(Long couponId) {
    return COUPON_RETRY_QUEUE_PREFIX + couponId;
  }

  public static String couponDeadLetterQueue(Long couponId) {
    return COUPON_DLQ_PREFIX + couponId;
  }
}
