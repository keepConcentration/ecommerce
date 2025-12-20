package com.phm.ecommerce.promotion.infrastructure.batch.config;

import com.phm.ecommerce.promotion.infrastructure.batch.quartz.CouponIssueQuartzJob;
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
