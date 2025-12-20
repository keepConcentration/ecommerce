package com.phm.ecommerce.common.domain.order;

import com.phm.ecommerce.common.domain.common.BaseEntity;
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

  @Column
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Column
  private String failureReason;

  protected Order() {
    super();
  }

  public Order(Long id, Long userId, Long totalAmount, Long discountAmount, Long finalAmount) {
    super(id);
    this.userId = userId;
    this.totalAmount = totalAmount;
    this.discountAmount = discountAmount;
    this.finalAmount = finalAmount;
    this.status = OrderStatus.PENDING;
  }

  public static Order create(Long userId, Long totalAmount, Long discountAmount) {
    Long actualDiscountAmount = Math.min(discountAmount, totalAmount);
    Long finalAmount = totalAmount - actualDiscountAmount;

    return new Order(null, userId, totalAmount, actualDiscountAmount, finalAmount);
  }

  public void complete() {
    this.status = OrderStatus.COMPLETED;
  }

  public void markAsFailed(String reason) {
    this.status = OrderStatus.FAILED;
    this.failureReason = reason;
  }

  public enum OrderStatus {
    PENDING,
    COMPLETED,
    FAILED
  }
}
