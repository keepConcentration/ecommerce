package com.phm.ecommerce.domain.product.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class InvalidDateRangeException extends BaseException {

  public InvalidDateRangeException() {
    super(ProductErrorCode.INVALID_DATE_RANGE);
  }
}
