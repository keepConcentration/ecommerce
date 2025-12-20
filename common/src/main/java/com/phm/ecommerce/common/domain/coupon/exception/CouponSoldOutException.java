package com.phm.ecommerce.common.domain.coupon.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class CouponSoldOutException extends BaseException {

  public CouponSoldOutException() {
    super(CouponErrorCode.COUPON_SOLD_OUT);
  }

  public CouponSoldOutException(Long couponId) {
    super(CouponErrorCode.COUPON_SOLD_OUT, "쿠폰이 모두 소진되었습니다. couponId: " + couponId);
  }
}
