package com.phm.ecommerce.domain.cart.exception;

import com.phm.ecommerce.domain.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
  CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", "장바구니 아이템이 존재하지 않습니다", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
