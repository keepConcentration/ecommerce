package com.phm.ecommerce.domain.coupon.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class CouponNotFoundException extends BaseException {

  public CouponNotFoundException() {
    super(CouponErrorCode.COUPON_NOT_FOUND);
  }

  public CouponNotFoundException(Long couponId) {
    super(CouponErrorCode.COUPON_NOT_FOUND, "쿠폰을 찾을 수 없습니다. couponId: " + couponId);
  }
}
