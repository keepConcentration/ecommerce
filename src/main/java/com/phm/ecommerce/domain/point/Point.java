package com.phm.ecommerce.domain.point;

import com.phm.ecommerce.domain.common.BaseEntity;
import lombok.Getter;

@Getter
public class Point extends BaseEntity {

  private Long userId;
  private Long amount;

  protected Point() {
    super();
  }

  private Point(Long id, Long userId, Long amount) {
    super(id);
    this.userId = userId;
    this.amount = amount != null ? amount : 0L;
  }

  public static Point create(Long userId) {
    return new Point(null, userId, 0L);
  }

  public static Point reconstruct(Long id, Long userId, Long amount) {
    return new Point(id, userId, amount);
  }

  public void charge(Long amount) {
    validateAmount(amount);
    this.amount += amount;
    updateTimestamp();
  }

  public void deduct(Long amount) {
    validateAmount(amount);
    if (this.amount < amount) {
      throw new IllegalStateException("포인트 잔액이 부족합니다");
    }
    this.amount -= amount;
    updateTimestamp();
  }

  public boolean hasEnough(Long requiredAmount) {
    return this.amount >= requiredAmount;
  }

  private void validateAmount(Long amount) {
    if (amount == null || amount <= 0) {
      throw new IllegalArgumentException("유효하지 않은 금액입니다");
    }
  }
}
