package com.phm.ecommerce.domain.order.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class InvalidOrderAmountException extends BaseException {

  public InvalidOrderAmountException(String fieldName) {
    super(OrderErrorCode.INVALID_AMOUNT, fieldName + "이(가) null일 수 없습니다");
  }

  public InvalidOrderAmountException(String fieldName, Long amount) {
    super(OrderErrorCode.INVALID_AMOUNT, fieldName + "은(는) 0보다 작을 수 없습니다. 현재 값: " + amount);
  }
}
