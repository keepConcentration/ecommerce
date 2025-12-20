package com.phm.ecommerce.order.infrastructure.batch.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.application.lock.DistributedLock;
import com.phm.ecommerce.common.application.lock.RedisLockKeys;
import com.phm.ecommerce.order.application.service.ExternalOrderService;
import com.phm.ecommerce.common.event.order.OrderCreatedEvent;
import com.phm.ecommerce.order.infrastructure.dlq.DeadLetterMessage;
import com.phm.ecommerce.order.infrastructure.dlq.RedisDLQService;
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
public class DLQRetryBatchJob {

  private final RedisDLQService dlqService;
  private final ExternalOrderService externalOrderService;
  private final ObjectMapper redisObjectMapper;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean
  public Job dlqRetryJob() {
    return new JobBuilder("dlqRetryJob", jobRepository)
        .start(dlqRetryStep())
        .build();
  }

  @Bean
  public Step dlqRetryStep() {
    return new StepBuilder("dlqRetryStep", jobRepository)
        .tasklet(dlqRetryTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet dlqRetryTasklet() {
    return (contribution, chunkContext) -> {
      retryFailedMessages();
      return RepeatStatus.FINISHED;
    };
  }

  @DistributedLock(lockKeyProvider = "prepareLockKey", waitTime = 10L, leaseTime = 60L)
  public void retryFailedMessages() {
    try {
      log.debug("DLQ 재시도 배치 시작");

      // 재시도 가능 메시지 목록 조회
      List<DeadLetterMessage> retryableMessages = dlqService.getRetryableMessages();

      if (retryableMessages.isEmpty()) {
        log.debug("재시도할 메시지 없음");
        return;
      }

      log.info("DLQ 재시도 시작 - 메시지 개수: {}", retryableMessages.size());

      int successCount = 0;
      int failureCount = 0;
      int exceededCount = 0;

      for (DeadLetterMessage message : retryableMessages) {
        try {
          if (!dlqService.canRetry(message)) {
            log.warn("DLQ 재시도 횟수 초과 - messageId: {}, retryCount: {}",
                message.id(), message.retryCount());
            dlqService.removeMessage(message.id());
            exceededCount++;
            continue;
          }

          OrderCreatedEvent event = redisObjectMapper.readValue(
              message.originalMessage(), OrderCreatedEvent.class);

          externalOrderService.sendOrderToExternalSystem(
              Long.parseLong(event.orderId()), event.userId(), event.finalAmount(), event.timestamp());

          dlqService.removeMessage(message.id());
          successCount++;

          log.info("DLQ 재시도 성공 - messageId: {}, orderId: {}, retryCount: {}",
              message.id(), event.orderId(), message.retryCount());

        } catch (JsonProcessingException e) {
          log.error("DLQ 메시지 파싱 실패 (제거) - messageId: {}", message.id(), e);
          dlqService.removeMessage(message.id());
          failureCount++;

        } catch (Exception e) {
          DeadLetterMessage updatedMessage = message.incrementRetryCount();
          dlqService.updateMessage(updatedMessage);
          failureCount++;

          log.warn("DLQ 재시도 실패 - messageId: {}, retryCount: {}, error: {}",
              updatedMessage.id(), updatedMessage.retryCount(), e.getMessage());
        }
      }

      log.info("DLQ 재시도 완료 - 성공: {}, 실패: {}, 초과: {}, 총: {}",
          successCount, failureCount, exceededCount, retryableMessages.size());

    } catch (Exception e) {
      log.error("DLQ 재시도 배치 실행 실패", e);
      throw e;
    }
  }

  private String prepareLockKey() {
    return RedisLockKeys.dlqRetry();
  }
}
