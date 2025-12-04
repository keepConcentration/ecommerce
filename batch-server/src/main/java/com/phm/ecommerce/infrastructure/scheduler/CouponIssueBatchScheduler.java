package com.phm.ecommerce.infrastructure.scheduler;

import com.phm.ecommerce.application.lock.DistributedLock;
import com.phm.ecommerce.application.lock.RedisLockKeys;
import com.phm.ecommerce.application.service.CouponIssueService;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.infrastructure.queue.CouponQueueService;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueBatchScheduler {

  private final CouponQueueService couponQueueService;
  private final CouponRepository couponRepository;
  private final CouponIssueService couponIssueService;

  private static final long BATCH_SIZE = 100L;

  @Value("${coupon.issue.max-retry-count:3}")
  private int maxRetryCount;

  @Scheduled(fixedDelay = 10000) // 10초마다 실행
  public void processAllCouponQueues() {
    log.debug("쿠폰 발급 배치 시작");

    // TODO: 발급 가능 쿠폰만 조회 등으로 리팩토링
    List<Coupon> coupons = couponRepository.findAll();

    for (Coupon coupon : coupons) {
      try {
        processCouponQueue(coupon.getId());

        processRetryQueue(coupon.getId());

        couponQueueService.moveToDeadLetterQueue(coupon.getId(), maxRetryCount);

      } catch (Exception e) {
        log.error("쿠폰 발급 배치 처리 중 오류 - couponId: {}", coupon.getId(), e);
      }
    }

    log.debug("쿠폰 발급 배치 완료");
  }

  @DistributedLock(lockKeyProvider = "prepareLockKey", waitTime = 5L, leaseTime = 30L)
  public void processCouponQueue(Long couponId) {
    long queueSize = couponQueueService.getQueueSize(couponId);

    if (queueSize == 0) {
      return;
    }

    log.info("쿠폰 발급 큐 처리 시작 - couponId: {}, queueSize: {}", couponId, queueSize);

    Set<ZSetOperations.TypedTuple<Object>> requests = couponQueueService.popOldestRequests(
        couponId, BATCH_SIZE);

    if (requests == null || requests.isEmpty()) {
      return;
    }

    int successCount = 0;
    int failCount = 0;
    boolean stockExhausted = false;

    for (ZSetOperations.TypedTuple<Object> request : requests) {
      String member = null;
      Long userId = null;

      try {
        member = (String) request.getValue();
        if (member == null) {
          continue;
        }

        userId = couponQueueService.extractUserId(member);
        Long extractedCouponId = couponQueueService.extractCouponId(member);

        Boolean issued = couponIssueService.issueCoupon(extractedCouponId, userId);

        if (issued == null) {
          stockExhausted = true;
          log.warn("쿠폰 재고 소진 - couponId: {}, 배치 처리 중단", couponId);
          break;
        } else if (issued) {
          successCount++;
        } else {
          failCount++;
        }

      } catch (Exception e) {
        log.error("개별 쿠폰 발급 중 오류 - couponId: {}, userId: {}", couponId, userId, e);
        failCount++;

        if (userId != null) {
          couponQueueService.addToRetryQueue(couponId, userId, 0);
        }
      }
    }

    if (stockExhausted) {
      couponQueueService.clearQueue(couponId);
      log.info("쿠폰 발급 재고 소진으로 큐 클리어 - couponId: {}, 성공: {}, 실패: {}",
          couponId, successCount, failCount);
    } else {
      log.info("쿠폰 발급 큐 처리 완료 - couponId: {}, 성공: {}, 실패: {}",
          couponId, successCount, failCount);
    }
  }

  @DistributedLock(lockKeyProvider = "prepareLockKey", waitTime = 5L, leaseTime = 30L)
  public void processRetryQueue(Long couponId) {
    long retryQueueSize = couponQueueService.getRetryQueueSize(couponId);

    if (retryQueueSize == 0) {
      return;
    }

    log.info("쿠폰 발급 재시도 큐 처리 시작 - couponId: {}, retryQueueSize: {}", couponId, retryQueueSize);

    Set<ZSetOperations.TypedTuple<Object>> retryRequests = couponQueueService.getRetryRequests(
        couponId, maxRetryCount, BATCH_SIZE);

    if (retryRequests == null || retryRequests.isEmpty()) {
      return;
    }

    int successCount = 0;
    int failCount = 0;
    boolean stockExhausted = false;

    for (ZSetOperations.TypedTuple<Object> request : retryRequests) {
      String member = null;
      Long userId = null;
      int currentRetryCount = 0;

      try {
        member = (String) request.getValue();
        if (member == null) {
          continue;
        }

        Double score = request.getScore();
        currentRetryCount = score != null ? score.intValue() : 0;

        userId = couponQueueService.extractUserId(member);
        Long extractedCouponId = couponQueueService.extractCouponId(member);

        Boolean issued = couponIssueService.issueCoupon(extractedCouponId, userId);

        if (issued == null) {
          stockExhausted = true;
          log.warn("쿠폰 재고 소진 - couponId: {}, 재시도 큐 처리 중단", couponId);
          couponQueueService.removeFromRetryQueue(couponId, userId);
          break;
        } else if (issued) {
          successCount++;
          couponQueueService.removeFromRetryQueue(couponId, userId);
        } else {
          failCount++;
          couponQueueService.removeFromRetryQueue(couponId, userId);
        }

      } catch (Exception e) {
        log.error("재시도 큐 쿠폰 발급 중 오류 - couponId: {}, userId: {}, retryCount: {}",
            couponId, userId, currentRetryCount, e);
        failCount++;

        if (userId != null) {
          int nextRetryCount = currentRetryCount + 1;
          couponQueueService.removeFromRetryQueue(couponId, userId);
          couponQueueService.addToRetryQueue(couponId, userId, nextRetryCount);

          if (nextRetryCount >= maxRetryCount) {
            log.warn("최대 재시도 횟수 도달 - couponId: {}, userId: {}, retryCount: {}",
                couponId, userId, nextRetryCount);
          }
        }
      }
    }

    if (stockExhausted) {
      log.info("쿠폰 발급 재시도 큐 처리 완료 (재고 소진) - couponId: {}, 성공: {}, 실패: {}",
          couponId, successCount, failCount);
    } else {
      log.info("쿠폰 발급 재시도 큐 처리 완료 - couponId: {}, 성공: {}, 실패: {}",
          couponId, successCount, failCount);
    }
  }

  private String prepareLockKey(Long couponId) {
    return RedisLockKeys.coupon(couponId);
  }
}
