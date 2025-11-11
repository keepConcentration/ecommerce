package com.phm.ecommerce.integration;

import com.phm.ecommerce.application.usecase.coupon.IssueCouponUseCase;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponSoldOutException;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.infrastructure.repository.UserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("쿠폰 동시 발급 통합 테스트")
class CouponConcurrencyIntegrationTest {

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
        } catch (CouponSoldOutException e) {
          failCount.incrementAndGet();
        } catch (Exception e) {
          e.printStackTrace();
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
    assertThat(coupon.getIssuedQuantity()).isEqualTo((long) TOTAL_QUANTITY);
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
        } catch (Exception e) {
          e.printStackTrace();
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
}
