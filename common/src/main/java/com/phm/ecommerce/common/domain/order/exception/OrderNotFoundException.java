package com.phm.ecommerce.common.domain.order.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class OrderNotFoundException extends BaseException {

  public OrderNotFoundException() {
    super(OrderErrorCode.ORDER_NOT_FOUND);
  }

  public OrderNotFoundException(Long orderId) {
    super(OrderErrorCode.ORDER_NOT_FOUND, "주문이 존재하지 않습니다. orderId: " + orderId);
  }
}
