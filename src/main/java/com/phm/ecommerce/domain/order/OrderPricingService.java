package com.phm.ecommerce.domain.order;

import com.phm.ecommerce.domain.order.exception.InvalidDiscountAmountException;
import com.phm.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

@Service
public class OrderPricingService {

  public Long calculateItemTotal(Product product, Long quantity) {
    validateProduct(product);
    validateQuantity(quantity);
    return product.getPrice() * quantity;
  }

  public Long calculateFinalAmount(Long totalAmount, Long discountAmount) {
    validateAmount(totalAmount, "총액");
    validateAmount(discountAmount, "할인 금액");

    Long finalAmount = totalAmount - discountAmount;

    if (finalAmount < 0) {
      throw new InvalidDiscountAmountException(totalAmount, discountAmount, finalAmount);
    }

    return finalAmount;
  }

  public Long sumAmounts(Long... itemAmounts) {
    Long total = 0L;
    for (Long amount : itemAmounts) {
      if (amount != null) {
        validateAmount(amount, "금액");
        total += amount;
      }
    }
    return total;
  }

  private void validateProduct(Product product) {
    if (product == null) {
      throw new IllegalArgumentException("상품 정보가 null일 수 없습니다");
    }
    if (product.getPrice() == null || product.getPrice() < 0) {
      throw new IllegalArgumentException("상품 가격이 유효하지 않습니다");
    }
  }

  private void validateQuantity(Long quantity) {
    if (quantity == null || quantity <= 0) {
      throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
    }
  }

  private void validateAmount(Long amount, String fieldName) {
    if (amount == null) {
      throw new IllegalArgumentException(fieldName + "이(가) null일 수 없습니다");
    }
    if (amount < 0) {
      throw new IllegalArgumentException(fieldName + "은(는) 0보다 작을 수 없습니다. 현재 값: " + amount);
    }
  }
}
