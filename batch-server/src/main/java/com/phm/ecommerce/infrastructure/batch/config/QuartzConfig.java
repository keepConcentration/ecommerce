package com.phm.ecommerce.infrastructure.batch.config;

import com.phm.ecommerce.infrastructure.batch.quartz.CouponIssueQuartzJob;
import com.phm.ecommerce.infrastructure.batch.quartz.DLQRetryQuartzJob;
import com.phm.ecommerce.infrastructure.batch.quartz.ProductRankingQuartzJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

  @Bean
  public JobDetail productRankingJobDetail() {
    return JobBuilder.newJob(ProductRankingQuartzJob.class)
        .withIdentity("productRankingJob")
        .withDescription("상품 인기 순위 업데이트 Job")
        .storeDurably()
        .build();
  }

  @Bean
  public Trigger productRankingJobTrigger() {
    // 10분마다 실행 (Cron: 매 10분마다)
    return TriggerBuilder.newTrigger()
        .forJob(productRankingJobDetail())
        .withIdentity("productRankingJobTrigger")
        .withDescription("10분마다 실행")
        .withSchedule(
            CronScheduleBuilder.cronSchedule("0 0/10 * * * ?")
                .withMisfireHandlingInstructionDoNothing()
        )
        .build();
  }

  @Bean
  public JobDetail dlqRetryJobDetail() {
    return JobBuilder.newJob(DLQRetryQuartzJob.class)
        .withIdentity("dlqRetryJob")
        .withDescription("DLQ 재시도 Job")
        .storeDurably()
        .build();
  }

  @Bean
  public Trigger dlqRetryJobTrigger() {
    // 1분마다 실행 (Cron: 매 1분마다)
    return TriggerBuilder.newTrigger()
        .forJob(dlqRetryJobDetail())
        .withIdentity("dlqRetryJobTrigger")
        .withDescription("1분마다 실행")
        .withSchedule(
            CronScheduleBuilder.cronSchedule("0 * * * * ?")
                .withMisfireHandlingInstructionFireAndProceed()
        )
        .build();
  }

  @Bean
  public JobDetail couponIssueJobDetail() {
    return JobBuilder.newJob(CouponIssueQuartzJob.class)
        .withIdentity("couponIssueJob")
        .withDescription("쿠폰 발급 큐 처리 Job")
        .storeDurably()
        .build();
  }

  @Bean
  public Trigger couponIssueJobTrigger() {
    // 10초마다 실행 (Cron: 매 10초마다)
    return TriggerBuilder.newTrigger()
        .forJob(couponIssueJobDetail())
        .withIdentity("couponIssueJobTrigger")
        .withDescription("10초마다 실행")
        .withSchedule(
            CronScheduleBuilder.cronSchedule("0/10 * * * * ?")
                .withMisfireHandlingInstructionFireAndProceed()
        )
        .build();
  }
}
