package com.phm.ecommerce.domain.cart;

import com.phm.ecommerce.domain.cart.exception.CartItemOwnershipViolationException;
import com.phm.ecommerce.domain.cart.exception.InvalidCartQuantityException;
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

  private CartItem(Long id, Long userId, Long productId, Long quantity) {
    super(id);
    validateQuantity(quantity);
    this.userId = userId;
    this.productId = productId;
    this.quantity = quantity;
  }

  public static CartItem create(Long userId, Long productId, Long quantity) {
    return new CartItem(null, userId, productId, quantity);
  }

  public static CartItem reconstruct(Long id, Long userId, Long productId, Long quantity) {
    return new CartItem(id, userId, productId, quantity);
  }

  public void updateQuantity(Long newQuantity) {
    validateQuantity(newQuantity);
    this.quantity = newQuantity;
    updateTimestamp();
  }

  public void increaseQuantity(Long quantity) {
    validateQuantity(quantity);
    this.quantity += quantity;
    updateTimestamp();
  }

  private static void validateQuantity(Long quantity) {
    if (quantity == null || quantity <= 0) {
      throw new InvalidCartQuantityException();
    }
  }

  public void validateOwnership(Long requestUserId) {
    if (!belongsTo(requestUserId)) {
      throw new CartItemOwnershipViolationException();
    }
  }

  public boolean belongsTo(Long userId) {
    return this.userId.equals(userId);
  }
}
