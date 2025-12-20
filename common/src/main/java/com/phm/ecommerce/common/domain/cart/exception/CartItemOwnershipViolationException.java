package com.phm.ecommerce.common.domain.cart.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class CartItemOwnershipViolationException extends BaseException {

  public CartItemOwnershipViolationException() {
    super(CartErrorCode.CART_ITEM_OWNERSHIP_VIOLATION);
  }
}
