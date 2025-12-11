package com.phm.ecommerce.infrastructure.batch.launcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueJobLauncher {

  private final JobLauncher jobLauncher;
  private final Job couponIssueJob;

  @Scheduled(fixedDelay = 10000) // 10초마다 실행
  public void launchCouponIssueJob() {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(couponIssueJob, jobParameters);

      log.debug("Coupon Issue Batch Job 실행 완료");

    } catch (Exception e) {
      log.error("Coupon Issue Batch Job 실행 실패", e);
    }
  }
}
