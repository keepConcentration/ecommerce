package com.phm.ecommerce.application.usecase.coupon;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyIssuedException;
import com.phm.ecommerce.domain.coupon.exception.CouponSoldOutException;
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

  public record Input(
      Long couponId,
      Long userId) {}

  // TODO 동시성 이슈 처리
  public Output execute(Input request) {
    Coupon coupon = couponRepository.findByIdOrThrow(request.couponId());

    boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(
        request.userId(), request.couponId());
    if (alreadyIssued) {
      throw new CouponAlreadyIssuedException();
    }

    if (!coupon.canIssue()) {
      throw new CouponSoldOutException();
    }
    coupon.issue();

    UserCoupon userCoupon = UserCoupon.issue(request.userId(), request.couponId(), coupon.getValidDays());
    userCoupon = userCouponRepository.save(userCoupon);

    couponRepository.save(coupon);

    return new Output(
        userCoupon.getId(),
        userCoupon.getUserId(),
        userCoupon.getCouponId(),
        coupon.getName(),
        coupon.getDiscountAmount(),
        userCoupon.getIssuedAt(),
        userCoupon.getExpiredAt()
    );
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
