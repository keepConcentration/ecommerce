package com.phm.ecommerce.domain.order.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class InvalidDiscountAmountException extends BaseException {

  public InvalidDiscountAmountException() {
    super(OrderErrorCode.INVALID_DISCOUNT_AMOUNT);
  }

  public InvalidDiscountAmountException(Long totalAmount, Long discountAmount, Long finalAmount) {
    super(
        OrderErrorCode.INVALID_DISCOUNT_AMOUNT,
        String.format(
            "할인 금액이 총 금액을 초과합니다. 총액: %d, 할인: %d, 최종: %d",
            totalAmount, discountAmount, finalAmount));
  }
}
