package com.phm.ecommerce.application.usecase.coupon;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyIssuedException;
import com.phm.ecommerce.domain.coupon.exception.CouponNotFoundException;
import com.phm.ecommerce.domain.coupon.exception.CouponSoldOutException;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import com.phm.ecommerce.presentation.dto.request.IssueCouponRequest;
import com.phm.ecommerce.presentation.dto.response.UserCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueCouponUseCase {

  private final CouponRepository couponRepository;
  private final UserCouponRepository userCouponRepository;

    // TODO 동시성 이슈 처리
  public UserCouponResponse execute(Long couponId, IssueCouponRequest request) {
    Coupon coupon = couponRepository.findById(couponId)
        .orElseThrow(CouponNotFoundException::new);

    boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(
        request.userId(), couponId);
    if (alreadyIssued) {
      throw new CouponAlreadyIssuedException();
    }

    if (!coupon.canIssue()) {
      throw new CouponSoldOutException();
    }
    coupon.issue();

    UserCoupon userCoupon = UserCoupon.issue(request.userId(), couponId, coupon.getValidDays());
    userCoupon = userCouponRepository.save(userCoupon);

    couponRepository.save(coupon);

    return new UserCouponResponse(
        userCoupon.getId(),
        userCoupon.getUserId(),
        userCoupon.getCouponId(),
        coupon.getName(),
        coupon.getDiscountAmount(),
        userCoupon.getIssuedAt(),
        userCoupon.getExpiredAt()
    );
  }
}
