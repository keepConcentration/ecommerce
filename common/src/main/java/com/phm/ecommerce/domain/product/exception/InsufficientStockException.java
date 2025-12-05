package com.phm.ecommerce.domain.product.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class InsufficientStockException extends BaseException {

  public InsufficientStockException() {
    super(ProductErrorCode.INSUFFICIENT_STOCK);
  }

  public InsufficientStockException(Long productId, Long requestedQuantity, Long availableQuantity) {
    super(
        ProductErrorCode.INSUFFICIENT_STOCK,
        String.format(
            "재고가 부족합니다. productId: %d, 요청 수량: %d, 재고 수량: %d",
            productId, requestedQuantity, availableQuantity));
  }
}
