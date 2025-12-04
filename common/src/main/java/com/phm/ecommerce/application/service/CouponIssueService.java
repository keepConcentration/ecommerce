package com.phm.ecommerce.application.service;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.infrastructure.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueService {

  private final CouponRepository couponRepository;
  private final UserCouponRepository userCouponRepository;

  @Transactional
  public boolean issueCoupon(Long couponId, Long userId) {
    try {
      boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(userId, couponId);
      if (alreadyIssued) {
        log.debug("이미 발급된 쿠폰 - userId: {}, couponId: {}", userId, couponId);
        return false;
      }

      Coupon coupon = couponRepository.findByIdOrThrow(couponId);
      if (!coupon.canIssue()) {
        log.warn("쿠폰 재고 소진 - couponId: {}, userId: {}", couponId, userId);
        return false;
      }

      coupon.issue();
      UserCoupon userCoupon = UserCoupon.issue(userId, coupon.getId(), coupon.getValidDays());

      userCouponRepository.save(userCoupon);
      couponRepository.save(coupon);

      log.info("쿠폰 발급 완료 - userId: {}, couponId: {}, userCouponId: {}",
          userId, couponId, userCoupon.getId());

      return true;

    } catch (DataIntegrityViolationException e) {
      log.warn("쿠폰 발급 중복 시도 - userId: {}, couponId: {}", userId, couponId);
      return false;
    } catch (Exception e) {
      log.error("쿠폰 발급 중 오류 - userId: {}, couponId: {}", userId, couponId, e);
      return false;
    }
  }
}
