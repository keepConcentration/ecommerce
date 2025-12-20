package com.phm.ecommerce.common.domain.point;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions", indexes = {
    @Index(name = "idx_point_transaction_point_id", columnList = "pointId"),
    @Index(name = "idx_point_transaction_order_id", columnList = "orderId")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
public class PointTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long pointId;

  @Column
  private Long orderId;

  @Column(nullable = false)
  private Long amount;

  @CreatedDate
  @Column(updatable = false)
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

  public static PointTransaction createRefund(Long pointId, Long orderId, Long amount) {
    return new PointTransaction(null, pointId, orderId, amount, null);
  }

  public boolean isCharge() {
    return this.amount > 0;
  }

  public boolean isDeduction() {
    return this.amount < 0;
  }
}
