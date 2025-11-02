package com.phm.ecommerce.domain.product.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class ProductNotFoundException extends BaseException {

  public ProductNotFoundException() {
    super(ProductErrorCode.PRODUCT_NOT_FOUND);
  }

  public ProductNotFoundException(Long productId) {
    super(ProductErrorCode.PRODUCT_NOT_FOUND, "상품이 존재하지 않습니다. productId: " + productId);
  }
}
