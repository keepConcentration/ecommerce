package com.phm.ecommerce.domain.cart;

import com.phm.ecommerce.domain.common.BaseEntity;
import lombok.Getter;

@Getter
public class CartItem extends BaseEntity {

  private Long userId;
  private Long productId;
  private Long quantity;

  protected CartItem() {
    super();
  }

  public CartItem(Long id, Long userId, Long productId, Long quantity) {
    super(id);
    this.userId = userId;
    this.productId = productId;
    this.quantity = quantity;
  }

  public static CartItem create(Long userId, Long productId, Long quantity) {
    validateQuantity(quantity);
    return new CartItem(null, userId, productId, quantity);
  }

  public void updateQuantity(Long newQuantity) {
    validateQuantity(newQuantity);
    this.quantity = newQuantity;
    updateTimestamp();
  }

  public void increaseQuantity(Long amount) {
    validateQuantity(amount);
    this.quantity += amount;
    updateTimestamp();
  }

  private static void validateQuantity(Long quantity) {
    if (quantity == null || quantity <= 0) {
      throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
    }
  }
}
