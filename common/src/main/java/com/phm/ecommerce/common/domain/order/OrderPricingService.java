package com.phm.ecommerce.common.domain.order;

import com.phm.ecommerce.common.domain.order.exception.InvalidOrderAmountException;
import com.phm.ecommerce.common.domain.order.exception.InvalidOrderQuantityException;
import com.phm.ecommerce.common.domain.order.exception.InvalidProductInfoException;
import com.phm.ecommerce.common.domain.order.exception.InvalidProductPriceException;
import com.phm.ecommerce.common.domain.product.Product;
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

    Long actualDiscountAmount = Math.min(discountAmount, totalAmount);
    Long finalAmount = totalAmount - actualDiscountAmount;

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
      throw new InvalidProductInfoException();
    }
    if (product.getPrice() == null || product.getPrice() < 0) {
      throw new InvalidProductPriceException(product.getPrice());
    }
  }

  private void validateQuantity(Long quantity) {
    if (quantity == null || quantity <= 0) {
      throw new InvalidOrderQuantityException(quantity);
    }
  }

  private void validateAmount(Long amount, String fieldName) {
    if (amount == null) {
      throw new InvalidOrderAmountException(fieldName);
    }
    if (amount < 0) {
      throw new InvalidOrderAmountException(fieldName, amount);
    }
  }
}
