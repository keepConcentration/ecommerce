package com.phm.ecommerce.common.domain.cart.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class InvalidCartQuantityException extends BaseException {

  public InvalidCartQuantityException() {
    super(CartErrorCode.INVALID_QUANTITY);
  }
}
