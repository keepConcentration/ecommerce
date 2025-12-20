package com.phm.ecommerce.common.domain.coupon.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class CouponExpiredException extends BaseException {

  public CouponExpiredException() {
    super(CouponErrorCode.COUPON_EXPIRED);
  }

  public CouponExpiredException(Long userCouponId) {
    super(CouponErrorCode.COUPON_EXPIRED, "만료된 쿠폰입니다. userCouponId: " + userCouponId);
  }
}
