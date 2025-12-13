package com.phm.ecommerce.application.lock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisLockKeys {

  public static String product(Long productId) {
    return "product:" + productId;
  }

  public static String pointUser(Long userId) {
    return "point:user:" + userId;
  }

  public static String coupon(Long couponId) {
    return "coupon:" + couponId;
  }

  public static String rankingUpdate() {
    return "ranking:update";
  }

  public static String dlqRetry() {
    return "dlq:retry";
  }
}
