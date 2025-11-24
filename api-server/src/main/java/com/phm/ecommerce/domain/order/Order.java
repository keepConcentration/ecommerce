package com.phm.ecommerce.domain.order;

import com.phm.ecommerce.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "userId")
})
@Getter
public class Order extends BaseEntity {

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long totalAmount;

  @Column(nullable = false)
  private Long discountAmount;

  @Column(nullable = false)
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
