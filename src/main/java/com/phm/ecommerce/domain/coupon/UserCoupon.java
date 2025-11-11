package com.phm.ecommerce.domain.coupon;

import com.phm.ecommerce.domain.common.BaseEntity;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyUsedException;
import com.phm.ecommerce.domain.coupon.exception.CouponExpiredException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCoupon extends BaseEntity {

  private Long userId;
  private Long couponId;
  private LocalDateTime issuedAt;
  private LocalDateTime usedAt;
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
    updateTimestamp();
  }

  public void rollbackUsage() {
    this.usedAt = null;
    updateTimestamp();
  }

  public Long calculateDiscount(Coupon coupon) {
    validateUsable();
    return coupon.getDiscountAmount();
  }
}
