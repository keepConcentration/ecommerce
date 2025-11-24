package com.phm.ecommerce.domain.order.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class EmptyCartException extends BaseException {

  public EmptyCartException() {
    super(OrderErrorCode.EMPTY_CART);
  }
}
