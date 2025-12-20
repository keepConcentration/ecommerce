package com.phm.ecommerce.common.domain.order.exception;

import com.phm.ecommerce.common.domain.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
  ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문이 존재하지 않습니다", HttpStatus.NOT_FOUND),
  EMPTY_CART("EMPTY_CART", "주문할 장바구니 아이템이 없습니다", HttpStatus.BAD_REQUEST),
  INVALID_DISCOUNT_AMOUNT("INVALID_DISCOUNT_AMOUNT", "할인 금액이 총 금액을 초과할 수 없습니다", HttpStatus.BAD_REQUEST),
  INVALID_PRODUCT_INFO("INVALID_PRODUCT_INFO", "상품 정보가 없습니다", HttpStatus.BAD_REQUEST),
  INVALID_PRODUCT_PRICE("INVALID_PRODUCT_PRICE", "상품 가격이 유효하지 않습니다", HttpStatus.BAD_REQUEST),
  INVALID_QUANTITY("INVALID_QUANTITY", "수량은 1 이상이어야 합니다", HttpStatus.BAD_REQUEST),
  INVALID_AMOUNT("INVALID_AMOUNT", "금액이 유효하지 않습니다", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
