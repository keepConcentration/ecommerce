package com.phm.ecommerce.common.domain.coupon.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class CouponNotFoundException extends BaseException {

  public CouponNotFoundException() {
    super(CouponErrorCode.COUPON_NOT_FOUND);
  }

  public CouponNotFoundException(Long couponId) {
    super(CouponErrorCode.COUPON_NOT_FOUND, "쿠폰을 찾을 수 없습니다. couponId: " + couponId);
  }
}
