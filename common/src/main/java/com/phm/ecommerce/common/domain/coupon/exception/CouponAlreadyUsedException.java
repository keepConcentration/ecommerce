package com.phm.ecommerce.common.domain.coupon.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class CouponAlreadyUsedException extends BaseException {

  public CouponAlreadyUsedException() {
    super(CouponErrorCode.COUPON_ALREADY_USED);
  }

  public CouponAlreadyUsedException(Long userCouponId) {
    super(CouponErrorCode.COUPON_ALREADY_USED, "이미 사용된 쿠폰입니다. userCouponId: " + userCouponId);
  }
}
