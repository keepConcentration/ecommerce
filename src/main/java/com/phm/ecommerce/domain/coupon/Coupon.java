package com.phm.ecommerce.domain.coupon;

import lombok.Getter;

@Getter
public class Coupon {

  private Long id;
  private String name;
  private Long discountAmount;
  private Long totalQuantity;
  private Long issuedQuantity;
  private Integer validDays;

  protected Coupon() {
    this.issuedQuantity = 0L;
  }

  public Coupon(
      Long id,
      String name,
      Long discountAmount,
      Long totalQuantity,
      Long issuedQuantity,
      Integer validDays) {
    this.id = id;
    this.name = name;
    this.discountAmount = discountAmount;
    this.totalQuantity = totalQuantity;
    this.issuedQuantity = issuedQuantity != null ? issuedQuantity : 0L;
    this.validDays = validDays;
  }

  public static Coupon create(String name, Long discountAmount, Long totalQuantity, Integer validDays) {
    return new Coupon(null, name, discountAmount, totalQuantity, 0L, validDays);
  }

  public boolean canIssue() {
    return this.issuedQuantity < this.totalQuantity;
  }

  public void issue() {
    if (!canIssue()) {
      throw new IllegalStateException("쿠폰이 모두 소진되었습니다");
    }
    this.issuedQuantity++;
  }

  public Long getRemainingQuantity() {
    return this.totalQuantity - this.issuedQuantity;
  }
}
