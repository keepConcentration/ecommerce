package com.phm.ecommerce.common.domain.order.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class InvalidProductInfoException extends BaseException {

  public InvalidProductInfoException() {
    super(OrderErrorCode.INVALID_PRODUCT_INFO);
  }

  public InvalidProductInfoException(String message) {
    super(OrderErrorCode.INVALID_PRODUCT_INFO, message);
  }
}
