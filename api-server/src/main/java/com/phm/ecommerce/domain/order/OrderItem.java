package com.phm.ecommerce.domain.order;

import com.phm.ecommerce.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "orderId"),
    @Index(name = "idx_order_item_user_id", columnList = "userId")
})
@Getter
public class OrderItem extends BaseEntity {

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long productId;

  @Column
  private Long userCouponId;

  @Column(nullable = false)
  private String productName;

  @Column(nullable = false)
  private Long quantity;

  @Column(nullable = false)
  private Long price;

  @Column(nullable = false)
  private Long totalPrice;

  @Column(nullable = false)
  private Long discountAmount;

  @Column(nullable = false)
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

    Long actualDiscountAmount = Math.min(discountAmount, totalPrice);
    Long finalAmount = totalPrice - actualDiscountAmount;

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
        actualDiscountAmount,
        finalAmount);
  }
}
