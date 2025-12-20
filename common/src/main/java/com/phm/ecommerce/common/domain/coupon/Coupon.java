package com.phm.ecommerce.common.domain.coupon;

import com.phm.ecommerce.common.domain.common.BaseEntity;
import com.phm.ecommerce.common.domain.coupon.exception.CouponSoldOutException;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "coupons")
@Getter
public class Coupon extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Long discountAmount;

  @Column(nullable = false)
  private Long totalQuantity;

  @Column(nullable = false)
  private Long issuedQuantity;

  @Column(nullable = false)
  private Integer validDays;

  protected Coupon() {
    super();
    this.issuedQuantity = 0L;
  }

  private Coupon(
      Long id,
      String name,
      Long discountAmount,
      Long totalQuantity,
      Long issuedQuantity,
      Integer validDays) {
    super(id);
    this.name = name;
    this.discountAmount = discountAmount;
    this.totalQuantity = totalQuantity;
    this.issuedQuantity = issuedQuantity != null ? issuedQuantity : 0L;
    this.validDays = validDays;
  }

  public static Coupon create(String name, Long discountAmount, Long totalQuantity, Integer validDays) {
    return new Coupon(null, name, discountAmount, totalQuantity, 0L, validDays);
  }

  public static Coupon reconstruct(
      Long id,
      String name,
      Long discountAmount,
      Long totalQuantity,
      Long issuedQuantity,
      Integer validDays) {
    return new Coupon(id, name, discountAmount, totalQuantity, issuedQuantity, validDays);
  }

  public boolean canIssue() {
    return this.issuedQuantity < this.totalQuantity;
  }

  public void issue() {
    if (!canIssue()) {
      throw new CouponSoldOutException();
    }
    this.issuedQuantity++;
  }

  public Long getRemainingQuantity() {
    return this.totalQuantity - this.issuedQuantity;
  }
}
