package com.phm.ecommerce.common.domain.coupon.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class UserCouponNotFoundException extends BaseException {

  public UserCouponNotFoundException() {
    super(CouponErrorCode.USER_COUPON_NOT_FOUND);
  }

  public UserCouponNotFoundException(Long userCouponId) {
    super(CouponErrorCode.USER_COUPON_NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다. userCouponId: " + userCouponId);
  }
}
