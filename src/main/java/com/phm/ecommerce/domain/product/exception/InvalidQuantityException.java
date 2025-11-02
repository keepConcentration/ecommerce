package com.phm.ecommerce.domain.product.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class InvalidQuantityException extends BaseException {

  public InvalidQuantityException() {
    super(ProductErrorCode.INVALID_QUANTITY);
  }

  public InvalidQuantityException(Long quantity) {
    super(ProductErrorCode.INVALID_QUANTITY, "유효하지 않은 수량입니다. quantity: " + quantity);
  }
}
