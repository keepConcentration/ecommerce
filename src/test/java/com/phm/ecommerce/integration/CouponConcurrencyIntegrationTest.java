package com.phm.ecommerce.integration;

import com.phm.ecommerce.application.usecase.coupon.IssueCouponUseCase;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyIssuedException;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.infrastructure.repository.UserCouponRepository;
import com.phm.ecommerce.support.TestContainerSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("쿠폰 동시 발급 통합 테스트")
class CouponConcurrencyIntegrationTest extends TestContainerSupport {

  @Autowired
  private IssueCouponUseCase issueCouponUseCase;

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  private UserCouponRepository userCouponRepository;

  private Long couponId;
  private static final int TOTAL_QUANTITY = 10;
  private static final int TOTAL_USERS = 100;

  @BeforeEach
  void setUp() {
    // 10개 한정 쿠폰 생성
    Coupon coupon = Coupon.create(
        "선착순 쿠폰",
        10000L,
        (long) TOTAL_QUANTITY,
        30
    );
    coupon = couponRepository.save(coupon);
    couponId = coupon.getId();
  }

  @AfterEach
  void tearDown() {
    userCouponRepository.deleteAll();
    couponRepository.deleteAll();
  }

  @Test
  @DisplayName("100명이 동시에 10개 한정 쿠폰 발급 - 10명 성공")
  void concurrentCouponIssuance_shouldIssueExactlyTenCoupons() throws InterruptedException {
    // given
    ExecutorService executorService = Executors.newFixedThreadPool(TOTAL_USERS);
    CountDownLatch latch = new CountDownLatch(TOTAL_USERS);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    // when
    for (long userId = 1; userId <= TOTAL_USERS; userId++) {
      long finalUserId = userId;
      executorService.submit(() -> {
        try {
          issueCouponUseCase.execute(new IssueCouponUseCase.Input(couponId, finalUserId));
          successCount.incrementAndGet();
        } catch (Exception e) {
          failCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // then
    assertThat(successCount.get()).isEqualTo(TOTAL_QUANTITY);
    assertThat(failCount.get()).isEqualTo(TOTAL_USERS - TOTAL_QUANTITY);

    List<UserCoupon> issuedCoupons = userCouponRepository.findByCouponId(couponId);
    assertThat(issuedCoupons).hasSize(TOTAL_QUANTITY);

    Coupon coupon = couponRepository.findByIdOrThrow(couponId);
    assertThat(coupon.getIssuedQuantity()).isEqualTo(TOTAL_QUANTITY);
    assertThat(coupon.getRemainingQuantity()).isZero();
  }

  @Test
  @DisplayName("50명이 동시에 100개 쿠폰 발급 - 50명 성공")
  void concurrentCouponIssuance_whenEnoughStock_shouldIssueAll() throws InterruptedException {
    // given
    Coupon coupon = Coupon.create("충분한 재고 쿠폰", 5000L, 100L, 30);
    coupon = couponRepository.save(coupon);
    Long abundantCouponId = coupon.getId();

    ExecutorService executorService = Executors.newFixedThreadPool(50);
    CountDownLatch latch = new CountDownLatch(50);
    AtomicInteger successCount = new AtomicInteger(0);

    // when
    for (long userId = 1; userId <= 50; userId++) {
      long finalUserId = userId;
      executorService.submit(() -> {
        try {
          issueCouponUseCase.execute(new IssueCouponUseCase.Input(abundantCouponId, finalUserId));
          successCount.incrementAndGet();
        } catch (Exception ignored) {

        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // then
    assertThat(successCount.get()).isEqualTo(50);

    Coupon result = couponRepository.findByIdOrThrow(abundantCouponId);
    assertThat(result.getIssuedQuantity()).isEqualTo(50L);
    assertThat(result.getRemainingQuantity()).isEqualTo(50L);
  }

  @Test
  @DisplayName("같은 사용자가 동시에 동일 쿠폰 10번 발급 시도 - 1번만 성공")
  void concurrentCouponIssuance_sameUserSameCoupon_shouldIssueOnlyOnce() throws InterruptedException {
    // given
    Long userId = 1L;
    int attemptCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
    CountDownLatch latch = new CountDownLatch(attemptCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger alreadyIssuedCount = new AtomicInteger(0);

    // when
    for (int i = 0; i < attemptCount; i++) {
      executorService.submit(() -> {
        try {
          issueCouponUseCase.execute(new IssueCouponUseCase.Input(couponId, userId));
          successCount.incrementAndGet();
        } catch (CouponAlreadyIssuedException e) {
          alreadyIssuedCount.incrementAndGet();
        } catch (Exception ignored) {

        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // then
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(alreadyIssuedCount.get()).isEqualTo(attemptCount - 1);

    List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
    assertThat(userCoupons).hasSize(1);
    assertThat(userCoupons.get(0).getCouponId()).isEqualTo(couponId);
  }
}
