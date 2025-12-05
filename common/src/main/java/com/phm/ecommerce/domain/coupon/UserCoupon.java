package com.phm.ecommerce.domain.coupon;

import com.phm.ecommerce.domain.common.BaseEntity;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyUsedException;
import com.phm.ecommerce.domain.coupon.exception.CouponExpiredException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons",
    indexes = {
        @Index(name = "idx_user_coupon_user_id", columnList = "userId"),
        @Index(name = "idx_user_coupon_coupon_id", columnList = "couponId")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_coupon", columnNames = {"userId", "couponId"})
    }
)
@Getter
public class UserCoupon extends BaseEntity {

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long couponId;

  @Column(nullable = false)
  private LocalDateTime issuedAt;

  @Column
  private LocalDateTime usedAt;

  @Column(nullable = false)
  private LocalDateTime expiredAt;

  protected UserCoupon() {
    super();
  }

  public UserCoupon(
      Long id,
      Long userId,
      Long couponId,
      LocalDateTime issuedAt,
      LocalDateTime usedAt,
      LocalDateTime expiredAt) {
    super(id);
    this.userId = userId;
    this.couponId = couponId;
    this.issuedAt = issuedAt;
    this.usedAt = usedAt;
    this.expiredAt = expiredAt;
  }

  public static UserCoupon issue(Long userId, Long couponId, Integer validDays) {
    LocalDateTime issuedAt = LocalDateTime.now();
    LocalDateTime expiredAt = issuedAt.plusDays(validDays);

    return new UserCoupon(null, userId, couponId, issuedAt, null, expiredAt);
  }

  public boolean isUsable() {
    return !isUsed() && !isExpired();
  }

  public boolean isUsed() {
    return this.usedAt != null;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiredAt);
  }

  public void validateUsable() {
    if (isUsed()) {
      throw new CouponAlreadyUsedException();
    }
    if (isExpired()) {
      throw new CouponExpiredException();
    }
  }

  public void use() {
    validateUsable();
    this.usedAt = LocalDateTime.now();
  }

  public void rollbackUsage() {
    this.usedAt = null;
  }

  public Long calculateDiscount(Coupon coupon) {
    validateUsable();
    return coupon.getDiscountAmount();
  }
}
