package com.phm.ecommerce.domain.product.exception;

import com.phm.ecommerce.domain.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
  PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품이 존재하지 않습니다", HttpStatus.NOT_FOUND),
  INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "재고가 부족합니다", HttpStatus.CONFLICT),
  INVALID_QUANTITY("INVALID_QUANTITY", "유효하지 않은 수량입니다", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
