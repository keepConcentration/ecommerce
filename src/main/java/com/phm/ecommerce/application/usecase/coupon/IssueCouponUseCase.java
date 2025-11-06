package com.phm.ecommerce.application.usecase.coupon;

import com.phm.ecommerce.application.lock.LockManager;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyIssuedException;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
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
    log.info("쿠폰 발급 시작 - userId: {}, couponId: {}", request.userId(), request.couponId());
    String lockKey = "coupon:" + request.couponId();

    return lockManager.executeWithLock(lockKey, () -> {
      boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(
          request.userId(), request.couponId());
      if (alreadyIssued) {
        log.warn("쿠폰 발급 실패 - 이미 발급된 쿠폰. userId: {}, couponId: {}",
            request.userId(), request.couponId());
        throw new CouponAlreadyIssuedException();
      }

      Coupon coupon = couponRepository.findByIdOrThrow(request.couponId());
      UserCoupon userCoupon = issue(request.userId(), coupon);

      log.info("쿠폰 발급 완료 - userCouponId: {}, userId: {}, couponId: {}",
          userCoupon.getId(), request.userId(), request.couponId());

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
