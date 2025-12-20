package com.phm.ecommerce.common.domain.cart.exception;

import com.phm.ecommerce.common.domain.common.exception.BaseException;

public class CartItemNotFoundException extends BaseException {

  public CartItemNotFoundException() {
    super(CartErrorCode.CART_ITEM_NOT_FOUND);
  }

  public CartItemNotFoundException(Long cartItemId) {
    super(CartErrorCode.CART_ITEM_NOT_FOUND, "장바구니 아이템이 존재하지 않습니다. cartItemId: " + cartItemId);
  }
}
