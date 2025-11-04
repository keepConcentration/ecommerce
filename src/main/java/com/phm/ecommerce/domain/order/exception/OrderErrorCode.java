package com.phm.ecommerce.domain.order.exception;

import com.phm.ecommerce.domain.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
  ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문이 존재하지 않습니다", HttpStatus.NOT_FOUND),
  INVALID_DISCOUNT_AMOUNT("INVALID_DISCOUNT_AMOUNT", "할인 금액이 총 금액을 초과할 수 없습니다", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
