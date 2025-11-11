package com.phm.ecommerce.domain.coupon.exception;

import com.phm.ecommerce.domain.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {
  COUPON_NOT_FOUND("COUPON_NOT_FOUND", "쿠폰을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
  COUPON_SOLD_OUT("COUPON_SOLD_OUT", "쿠폰이 모두 소진되었습니다", HttpStatus.CONFLICT),
  COUPON_ALREADY_ISSUED("COUPON_ALREADY_ISSUED", "이미 발급받은 쿠폰입니다", HttpStatus.CONFLICT),
  COUPON_EXPIRED("COUPON_EXPIRED", "만료된 쿠폰입니다", HttpStatus.BAD_REQUEST),
  COUPON_ALREADY_USED("COUPON_ALREADY_USED", "이미 사용된 쿠폰입니다", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
