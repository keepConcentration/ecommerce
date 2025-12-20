package com.phm.ecommerce.common.domain.order.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class InvalidProductPriceException extends BaseException {

  public InvalidProductPriceException() {
    super(OrderErrorCode.INVALID_PRODUCT_PRICE);
  }

  public InvalidProductPriceException(Long price) {
    super(OrderErrorCode.INVALID_PRODUCT_PRICE, "상품 가격이 유효하지 않습니다. 가격: " + price);
  }
}
