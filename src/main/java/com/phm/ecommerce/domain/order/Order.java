package com.phm.ecommerce.domain.order;

import com.phm.ecommerce.domain.common.BaseEntity;
import lombok.Getter;

@Getter
public class Order extends BaseEntity {

  private Long userId;
  private Long totalAmount;
  private Long discountAmount;
  private Long finalAmount;

  protected Order() {
    super();
  }

  public Order(Long id, Long userId, Long totalAmount, Long discountAmount, Long finalAmount) {
    super(id);
    this.userId = userId;
    this.totalAmount = totalAmount;
    this.discountAmount = discountAmount;
    this.finalAmount = finalAmount;
  }

  public static Order create(Long userId, Long totalAmount, Long discountAmount) {
    Long actualDiscountAmount = Math.min(discountAmount, totalAmount);
    Long finalAmount = totalAmount - actualDiscountAmount;

    return new Order(null, userId, totalAmount, actualDiscountAmount, finalAmount);
  }
}
