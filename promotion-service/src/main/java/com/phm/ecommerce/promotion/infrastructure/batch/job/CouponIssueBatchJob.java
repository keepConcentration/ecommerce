package com.phm.ecommerce.promotion.infrastructure.batch.job;

import com.phm.ecommerce.promotion.application.service.CouponIssueBatchService;
import com.phm.ecommerce.common.domain.coupon.Coupon;
import com.phm.ecommerce.common.infrastructure.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CouponIssueBatchJob {

  private final CouponRepository couponRepository;
  private final CouponIssueBatchService couponIssueBatchService;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean
  public Job couponIssueJob() {
    return new JobBuilder("couponIssueJob", jobRepository)
        .start(couponIssueStep())
        .build();
  }

  @Bean
  public Step couponIssueStep() {
    return new StepBuilder("couponIssueStep", jobRepository)
        .tasklet(couponIssueTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet couponIssueTasklet() {
    return (contribution, chunkContext) -> {
      processAllCouponQueues();
      return RepeatStatus.FINISHED;
    };
  }

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
