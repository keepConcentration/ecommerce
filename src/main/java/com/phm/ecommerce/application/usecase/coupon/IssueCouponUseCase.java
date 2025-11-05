package com.phm.ecommerce.application.usecase.coupon;

import com.phm.ecommerce.application.lock.LockManager;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyIssuedException;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IssueCouponUseCase {

  private final CouponRepository couponRepository;
  private final UserCouponRepository userCouponRepository;
  private final LockManager lockManager;

  public record Input(
      Long couponId,
      Long userId) {}

  public Output execute(Input request) {
    String lockKey = "coupon:" + request.couponId();

    return lockManager.executeWithLock(lockKey, () -> {
      boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(
          request.userId(), request.couponId());
      if (alreadyIssued) {
        throw new CouponAlreadyIssuedException();
      }

      Coupon coupon = couponRepository.findByIdOrThrow(request.couponId());
      UserCoupon userCoupon = issue(request.userId(), coupon);

      return new Output(
          userCoupon.getId(),
          userCoupon.getUserId(),
          userCoupon.getCouponId(),
          coupon.getName(),
          coupon.getDiscountAmount(),
          userCoupon.getIssuedAt(),
          userCoupon.getExpiredAt()
      );
    });
  }

  private UserCoupon issue(Long userId, Coupon coupon) {
      coupon.issue();
      UserCoupon userCoupon = UserCoupon.issue(userId, coupon.getId(), coupon.getValidDays());
      userCoupon = userCouponRepository.save(userCoupon);
      couponRepository.save(coupon);
      return userCoupon;
  }

  public record Output(
      Long userCouponId,
      Long userId,
      Long couponId,
      String couponName,
      Long discountAmount,
      LocalDateTime issuedAt,
      LocalDateTime expiredAt) {}
}
