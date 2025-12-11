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
public class DLQRetryJobLauncher {

  private final JobLauncher jobLauncher;
  private final Job dlqRetryJob;

  @Scheduled(fixedDelay = 60000, initialDelay = 30000)
  public void launchDLQRetryJob() {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(dlqRetryJob, jobParameters);

      log.debug("DLQ Retry Batch Job 실행 완료");

    } catch (Exception e) {
      log.error("DLQ Retry Batch Job 실행 실패", e);
    }
  }
}
