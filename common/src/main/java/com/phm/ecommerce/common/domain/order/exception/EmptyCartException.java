package com.phm.ecommerce.common.domain.order.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class EmptyCartException extends BaseException {

  public EmptyCartException() {
    super(OrderErrorCode.EMPTY_CART);
  }
}
