package com.phm.ecommerce.payment.integration;

import com.phm.ecommerce.payment.application.usecase.point.ChargePointsUseCase;
import com.phm.ecommerce.common.domain.point.Point;
import com.phm.ecommerce.common.domain.point.PointTransaction;
import com.phm.ecommerce.common.infrastructure.repository.PointRepository;
import com.phm.ecommerce.common.infrastructure.repository.PointTransactionRepository;
import com.phm.ecommerce.payment.support.TestContainerSupport;
import org.junit.jupiter.api.AfterEach;
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
@DisplayName("포인트 충전 동시성 통합 테스트")
class PointChargeConcurrencyIntegrationTest extends TestContainerSupport {

  @Autowired
  private ChargePointsUseCase chargePointsUseCase;

  @Autowired
  private PointRepository pointRepository;

  @Autowired
  private PointTransactionRepository pointTransactionRepository;

  @AfterEach
  void tearDown() {
    pointTransactionRepository.deleteAll();
    pointRepository.deleteAll();
  }

  @Test
  @DisplayName("같은 사용자가 동시에 10번 충전 - 모두 성공하고 정확한 금액 계산")
  void concurrentPointCharge_sameUser_shouldChargeAllCorrectly() throws InterruptedException {
    // given
    Long userId = 1L;
    long chargeAmount = 1000L;
    int attemptCount = 10;

    ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
    CountDownLatch latch = new CountDownLatch(attemptCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    // when
    for (int i = 0; i < attemptCount; i++) {
      executorService.submit(() -> {
        try {
          chargePointsUseCase.execute(new ChargePointsUseCase.Input(userId, chargeAmount));
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
    assertThat(successCount.get()).isEqualTo(attemptCount);
    assertThat(failCount.get()).isZero();

    Point point = pointRepository.findByUserIdOrThrow(userId);
    assertThat(point.getAmount()).isEqualTo(chargeAmount * attemptCount);

    List<PointTransaction> transactions = pointTransactionRepository.findByPointId(point.getId());
    assertThat(transactions).hasSize(attemptCount);
  }
}
