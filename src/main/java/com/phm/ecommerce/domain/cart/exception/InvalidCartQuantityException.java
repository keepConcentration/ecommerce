package com.phm.ecommerce.domain.cart.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class InvalidCartQuantityException extends BaseException {

  public InvalidCartQuantityException() {
    super(CartErrorCode.INVALID_QUANTITY);
  }
}
