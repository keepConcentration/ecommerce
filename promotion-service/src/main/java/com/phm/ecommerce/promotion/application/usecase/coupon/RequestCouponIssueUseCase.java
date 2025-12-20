package com.phm.ecommerce.promotion.application.usecase.coupon;

import com.phm.ecommerce.common.domain.coupon.exception.CouponAlreadyIssuedException;
import com.phm.ecommerce.promotion.infrastructure.queue.CouponQueueService;
import com.phm.ecommerce.common.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.common.infrastructure.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestCouponIssueUseCase {

  private final CouponRepository couponRepository;
  private final UserCouponRepository userCouponRepository;
  private final CouponQueueService couponQueueService;

  public record Input(
      Long couponId,
      Long userId) {}

  public Output execute(Input request) {
    log.info("비동기 쿠폰 발급 요청 - userId: {}, couponId: {}", request.userId(), request.couponId());
    boolean added = couponQueueService.addCouponRequest(request.couponId(), request.userId());

    if (!added) {
      log.warn("쿠폰 발급 요청 실패 - 이미 대기 중인 요청. userId: {}, couponId: {}",
          request.userId(), request.couponId());
      throw new CouponAlreadyIssuedException();
    }

    try {
      couponRepository.findByIdOrThrow(request.couponId());
      boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(
          request.userId(), request.couponId());
      if (alreadyIssued) {
        couponQueueService.removeCouponRequest(request.couponId(), request.userId());
        log.warn("쿠폰 발급 요청 실패 - 이미 발급된 쿠폰 userId: {}, couponId: {}",
            request.userId(), request.couponId());
        throw new CouponAlreadyIssuedException();
      }

      long queueSize = couponQueueService.getQueueSize(request.couponId());

      log.info("비동기 쿠폰 발급 요청 완료 - userId: {}, couponId: {}, queueSize: {}",
          request.userId(), request.couponId(), queueSize);

      return new Output();

    } catch (Exception e) {
      couponQueueService.removeCouponRequest(request.couponId(), request.userId());
      log.error("쿠폰 발급 요청 검증 실패 - 큐에서 제거. userId: {}, couponId: {}",
          request.userId(), request.couponId(), e);
      throw e;
    }
  }

  public record Output() {}
}
