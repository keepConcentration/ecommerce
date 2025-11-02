package com.phm.ecommerce.domain.point;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointTransaction {

  private Long id;
  private Long pointId;
  private Long orderId;
  private Long amount;
  private LocalDateTime createdAt;

  protected PointTransaction() {
    this.createdAt = LocalDateTime.now();
  }

  public PointTransaction(Long id, Long pointId, Long orderId, Long amount, LocalDateTime createdAt) {
    this.id = id;
    this.pointId = pointId;
    this.orderId = orderId;
    this.amount = amount;
    this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
  }

  public static PointTransaction createCharge(Long pointId, Long amount) {
    return new PointTransaction(null, pointId, null, amount, null);
  }

  public static PointTransaction createDeduction(Long pointId, Long orderId, Long amount) {
    return new PointTransaction(null, pointId, orderId, -amount, null);
  }

  public boolean isCharge() {
    return this.amount > 0;
  }

  public boolean isDeduction() {
    return this.amount < 0;
  }
}
