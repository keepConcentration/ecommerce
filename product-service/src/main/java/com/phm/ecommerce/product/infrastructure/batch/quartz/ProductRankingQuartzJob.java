package com.phm.ecommerce.product.infrastructure.batch.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRankingQuartzJob extends QuartzJobBean {

  private final JobLauncher jobLauncher;
  private final Job productRankingJob;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      log.info("Product Ranking Quartz Job 시작");

      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(productRankingJob, jobParameters);

      log.info("Product Ranking Quartz Job 완료");

    } catch (Exception e) {
      log.error("Product Ranking Quartz Job 실행 실패", e);
      throw new JobExecutionException("Product Ranking Job 실행 중 오류 발생", e);
    }
  }
}
