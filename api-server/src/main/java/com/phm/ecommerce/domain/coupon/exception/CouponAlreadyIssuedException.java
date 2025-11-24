package com.phm.ecommerce.domain.coupon.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class CouponAlreadyIssuedException extends BaseException {

  public CouponAlreadyIssuedException() {
    super(CouponErrorCode.COUPON_ALREADY_ISSUED);
  }

  public CouponAlreadyIssuedException(Long userId, Long couponId) {
    super(
        CouponErrorCode.COUPON_ALREADY_ISSUED,
        String.format("이미 발급받은 쿠폰입니다. userId: %d, couponId: %d", userId, couponId));
  }
}
