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
public class ProductRankingJobLauncher {

  private final JobLauncher jobLauncher;
  private final Job productRankingJob;

  @Scheduled(fixedDelay = 600000, initialDelay = 10000) // 시작 후 10초 후, 10분마다 실행
  public void launchProductRankingJob() {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis())  // 매 실행마다 고유한 파라미터
          .toJobParameters();

      jobLauncher.run(productRankingJob, jobParameters);

      log.info("Product Ranking Batch Job 실행 완료");

    } catch (Exception e) {
      log.error("Product Ranking Batch Job 실행 실패", e);
    }
  }
}
