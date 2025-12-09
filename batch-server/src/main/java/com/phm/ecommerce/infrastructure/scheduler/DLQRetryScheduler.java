package com.phm.ecommerce.infrastructure.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.application.lock.DistributedLock;
import com.phm.ecommerce.application.lock.RedisLockKeys;
import com.phm.ecommerce.application.service.ExternalOrderService;
import com.phm.ecommerce.domain.order.event.OrderCreatedEvent;
import com.phm.ecommerce.infrastructure.dlq.DeadLetterMessage;
import com.phm.ecommerce.infrastructure.dlq.RedisDLQService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DLQRetryScheduler {

  private final RedisDLQService dlqService;
  private final ExternalOrderService externalOrderService;
  private final ObjectMapper redisObjectMapper;

  @Scheduled(fixedDelay = 60000, initialDelay = 30000)
  @DistributedLock(lockKeyProvider = "prepareLockKey", waitTime = 10L, leaseTime = 60L)
  public void retryFailedMessages() {
    try {
      log.debug("DLQ 재시도 스케줄러 시작");

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
              event.orderId(), event.userId(), event.finalAmount(), event.createdAt());

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
      log.error("DLQ 재시도 스케줄러 실행 실패", e);
    }
  }

  private String prepareLockKey() {
    return RedisLockKeys.dlqRetry();
  }
}
