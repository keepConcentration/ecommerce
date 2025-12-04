package com.phm.ecommerce.infrastructure.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisCacheKeys {

  private static final String PRODUCT_PREFIX = "product:";
  private static final String COUPON_QUEUE_PREFIX = "coupon:queue:";
  private static final String COUPON_QUEUE_SEQUENCE_PREFIX = "coupon:queue:sequence:";

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
}
