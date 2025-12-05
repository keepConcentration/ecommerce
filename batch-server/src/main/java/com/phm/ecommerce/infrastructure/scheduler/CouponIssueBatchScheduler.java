package com.phm.ecommerce.infrastructure.scheduler;

import com.phm.ecommerce.application.service.CouponIssueBatchService;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueBatchScheduler {

  private final CouponRepository couponRepository;
  private final CouponIssueBatchService couponIssueBatchService;

  @Scheduled(fixedDelay = 10000) // 10초마다 실행
  public void processAllCouponQueues() {
    log.debug("쿠폰 발급 배치 시작");

    // TODO: 발급 가능 쿠폰만 조회 등으로 리팩토링
    List<Coupon> coupons = couponRepository.findAll();

    for (Coupon coupon : coupons) {
      try {
        couponIssueBatchService.processCouponIssueQueue(coupon.getId());
      } catch (Exception e) {
        log.error("쿠폰 발급 배치 처리 중 오류 - couponId: {}", coupon.getId(), e);
      }
    }

    log.debug("쿠폰 발급 배치 완료");
  }
}
