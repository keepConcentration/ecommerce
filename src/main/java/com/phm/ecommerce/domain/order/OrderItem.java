package com.phm.ecommerce.domain.order;

import com.phm.ecommerce.domain.common.BaseEntity;
import lombok.Getter;

@Getter
public class OrderItem extends BaseEntity {

  private Long orderId;
  private Long userId;
  private Long productId;
  private Long userCouponId;
  private String productName;
  private Long quantity;
  private Long price;
  private Long totalPrice;
  private Long discountAmount;
  private Long finalAmount;

  protected OrderItem() {
    super();
  }

  public OrderItem(
      Long id,
      Long orderId,
      Long userId,
      Long productId,
      Long userCouponId,
      String productName,
      Long quantity,
      Long price,
      Long totalPrice,
      Long discountAmount,
      Long finalAmount) {
    super(id);
    this.orderId = orderId;
    this.userId = userId;
    this.productId = productId;
    this.userCouponId = userCouponId;
    this.productName = productName;
    this.quantity = quantity;
    this.price = price;
    this.totalPrice = totalPrice;
    this.discountAmount = discountAmount;
    this.finalAmount = finalAmount;
  }

  public static OrderItem create(
      Long orderId,
      Long userId,
      Long productId,
      String productName,
      Long quantity,
      Long price,
      Long discountAmount,
      Long userCouponId) {
    Long totalPrice = price * quantity;
    Long finalAmount = totalPrice - discountAmount;

    if (finalAmount < 0) {
      throw new IllegalArgumentException("최종 금액은 0 이상이어야 합니다");
    }

    return new OrderItem(
        null,
        orderId,
        userId,
        productId,
        userCouponId,
        productName,
        quantity,
        price,
        totalPrice,
        discountAmount,
        finalAmount);
  }
}
