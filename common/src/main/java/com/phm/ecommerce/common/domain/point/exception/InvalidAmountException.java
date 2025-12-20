package com.phm.ecommerce.common.domain.point.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class InvalidAmountException extends BaseException {

  public InvalidAmountException() {
    super(PointErrorCode.INVALID_AMOUNT);
  }

  public InvalidAmountException(Long amount) {
    super(PointErrorCode.INVALID_AMOUNT, "유효하지 않은 금액입니다. amount: " + amount);
  }
}
