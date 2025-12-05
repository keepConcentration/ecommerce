package com.phm.ecommerce.domain.cart.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class CartItemOwnershipViolationException extends BaseException {

  public CartItemOwnershipViolationException() {
    super(CartErrorCode.CART_ITEM_OWNERSHIP_VIOLATION);
  }
}
