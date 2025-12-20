package com.phm.ecommerce.common.domain.order.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class InvalidOrderQuantityException extends BaseException {

  public InvalidOrderQuantityException() {
    super(OrderErrorCode.INVALID_QUANTITY);
  }

  public InvalidOrderQuantityException(Long quantity) {
    super(OrderErrorCode.INVALID_QUANTITY, "수량은 1 이상이어야 합니다. 현재 값: " + quantity);
  }
}
