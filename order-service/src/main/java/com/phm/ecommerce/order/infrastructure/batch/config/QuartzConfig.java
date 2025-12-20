package com.phm.ecommerce.order.infrastructure.batch.config;

import com.phm.ecommerce.order.infrastructure.batch.quartz.DLQRetryQuartzJob;
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
}
