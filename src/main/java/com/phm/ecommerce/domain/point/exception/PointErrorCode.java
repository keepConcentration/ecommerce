package com.phm.ecommerce.domain.point.exception;

import com.phm.ecommerce.domain.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {
  INSUFFICIENT_POINTS("INSUFFICIENT_POINTS", "포인트가 부족합니다", HttpStatus.CONFLICT),
  INVALID_AMOUNT("INVALID_AMOUNT", "유효하지 않은 금액입니다", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
