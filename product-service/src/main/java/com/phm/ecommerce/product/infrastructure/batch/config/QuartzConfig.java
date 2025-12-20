package com.phm.ecommerce.product.infrastructure.batch.config;

import com.phm.ecommerce.product.infrastructure.batch.quartz.ProductRankingQuartzJob;
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
}
