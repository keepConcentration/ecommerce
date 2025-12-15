package com.phm.ecommerce.batch;

import com.phm.ecommerce.application.service.CouponIssueBatchService;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.infrastructure.queue.CouponQueueService;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.infrastructure.repository.UserCouponRepository;
import com.phm.ecommerce.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponIssueBatchJobTest extends TestContainerSupport {

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job couponIssueJob;

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  private UserCouponRepository userCouponRepository;

  @Autowired
  private CouponQueueService couponQueueService;

  @Autowired
  private CouponIssueBatchService couponIssueBatchService;

  @BeforeEach
  void setUp() {
    userCouponRepository.deleteAll();
    couponRepository.deleteAll();
    // 큐 초기화는 각 쿠폰별로 하므로 여기서는 생략
  }

  @Test
  @DisplayName("쿠폰 발급 배치가 성공적으로 실행되어야 한다")
  void couponIssueBatchJobShouldComplete() throws Exception {
    // Given: 쿠폰 생성 및 발급 요청 큐에 추가
    Coupon coupon = createCoupon("신년 특가 쿠폰", 100L);

    couponQueueService.addCouponRequest(coupon.getId(), 1L);
    couponQueueService.addCouponRequest(coupon.getId(), 2L);

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(couponIssueJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 쿠폰이 발급되어야 함
    long issuedCount = userCouponRepository.countByCouponId(coupon.getId());
    assertThat(issuedCount).isEqualTo(2);

    // And: 쿠폰 발급 수량이 증가해야 함
    Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
    assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(2);

    // And: 큐가 비어야 함
    assertThat(couponQueueService.getQueueSize(coupon.getId())).isEqualTo(0);
  }

  @Test
  @DisplayName("발급 요청이 없으면 아무 작업 없이 완료되어야 한다")
  void couponIssueBatchJobShouldCompleteWithNoRequests() throws Exception {
    // Given: 쿠폰만 생성 (발급 요청 없음)
    createCoupon("여름 할인 쿠폰", 100L);

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(couponIssueJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 쿠폰이 발급되지 않아야 함
    long totalIssued = userCouponRepository.count();
    assertThat(totalIssued).isEqualTo(0);
  }

  @Test
  @DisplayName("여러 쿠폰의 발급 요청을 배치로 처리해야 한다")
  void couponIssueBatchJobShouldProcessMultipleCoupons() throws Exception {
    // Given: 여러 쿠폰 생성 및 발급 요청
    Coupon coupon1 = createCoupon("쿠폰 A", 50L);
    Coupon coupon2 = createCoupon("쿠폰 B", 50L);

    // 쿠폰 A 발급 요청
    for (long userId = 1; userId <= 3; userId++) {
      couponQueueService.addCouponRequest(coupon1.getId(), userId);
    }

    // 쿠폰 B 발급 요청
    for (long userId = 4; userId <= 6; userId++) {
      couponQueueService.addCouponRequest(coupon2.getId(), userId);
    }

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(couponIssueJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 각 쿠폰이 정확히 발급되어야 함
    assertThat(userCouponRepository.countByCouponId(coupon1.getId())).isEqualTo(3);
    assertThat(userCouponRepository.countByCouponId(coupon2.getId())).isEqualTo(3);

    // And: 모든 큐가 비어야 함
    assertThat(couponQueueService.getQueueSize(coupon1.getId())).isEqualTo(0);
    assertThat(couponQueueService.getQueueSize(coupon2.getId())).isEqualTo(0);
  }

  @Test
  @DisplayName("쿠폰 수량이 소진되면 발급이 중단되어야 한다")
  void couponIssueBatchJobShouldStopWhenSoldOut() throws Exception {
    // Given: 수량이 제한된 쿠폰 생성
    Coupon coupon = createCoupon("한정 쿠폰", 2L);

    // And: 3개의 발급 요청 (수량보다 많음)
    for (long userId = 1; userId <= 3; userId++) {
      couponQueueService.addCouponRequest(coupon.getId(), userId);
    }

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(couponIssueJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 최대 수량만큼만 발급되어야 함
    long issuedCount = userCouponRepository.countByCouponId(coupon.getId());
    assertThat(issuedCount).isEqualTo(2);

    // And: 쿠폰이 소진되어야 함
    Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
    assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(updatedCoupon.getTotalQuantity());
  }

  @Test
  @DisplayName("동일 사용자에 대한 중복 발급 요청은 한 번만 처리되어야 한다")
  void couponIssueBatchJobShouldPreventDuplicateIssuance() throws Exception {
    // Given: 쿠폰 생성
    Coupon coupon = createCoupon("중복 방지 테스트 쿠폰", 100L);

    // And: 동일 사용자의 중복 발급 요청
    Long userId = 1L;
    couponQueueService.addCouponRequest(coupon.getId(), userId);
    couponQueueService.addCouponRequest(coupon.getId(), userId);
    couponQueueService.addCouponRequest(coupon.getId(), userId);

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(couponIssueJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 중복 발급이 방지되어야 함 (한 번만 발급)
    boolean exists = userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId());
    assertThat(exists).isTrue();

    long issuedCount = userCouponRepository.countByCouponId(coupon.getId());
    assertThat(issuedCount).isLessThanOrEqualTo(1);
  }

  private Coupon createCoupon(String name, Long totalQuantity) {
    Coupon coupon = Coupon.create(
        name,
        5000L,  // discountAmount
        totalQuantity,
        30  // validDays
    );
    return couponRepository.save(coupon);
  }
}
