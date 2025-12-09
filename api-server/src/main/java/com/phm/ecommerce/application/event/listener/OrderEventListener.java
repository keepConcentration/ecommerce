package com.phm.ecommerce.application.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.application.service.ExternalOrderService;
import com.phm.ecommerce.domain.order.event.OrderCreatedEvent;
import com.phm.ecommerce.infrastructure.dlq.DeadLetterMessage;
import com.phm.ecommerce.infrastructure.dlq.RedisDLQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

  private final ExternalOrderService externalOrderService;
  private final RedisDLQService dlqService;
  private final ObjectMapper redisObjectMapper;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Retryable(
      retryFor = {Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void onOrderCreated(OrderCreatedEvent event) throws InterruptedException {
    log.info("주문 생성 이벤트 수신 - orderId: {}, userId: {}, finalAmount: {}, createdAt: {}",
        event.orderId(), event.userId(), event.finalAmount(), event.createdAt());

    externalOrderService.sendOrderToExternalSystem(
        event.orderId(), event.userId(), event.finalAmount(), event.createdAt());

    log.info("주문 생성 이벤트 처리 완료 - orderId: {}", event.orderId());
  }

  @Recover
  public void recoverOrderCreated(Exception e, OrderCreatedEvent event) {
    log.error("주문 생성 이벤트 처리 최종 실패 - orderId: {}, error: {}",
        event.orderId(), e.getMessage(), e);

    try {
      // 이벤트를 JSON으로 직렬화
      String originalMessage = redisObjectMapper.writeValueAsString(event);

      // DLQ 메시지 생성
      DeadLetterMessage dlqMessage = DeadLetterMessage.create(
          "order:" + event.orderId(),
          originalMessage,
          e.getClass().getName(),
          e.getMessage()
      );

      // DLQ에 저장
      dlqService.addToDeadLetterQueue(dlqMessage);

      log.info("주문 생성 이벤트 DLQ 저장 완료 - orderId: {}, messageId: {}",
          event.orderId(), dlqMessage.id());

    } catch (JsonProcessingException jsonEx) {
      log.error("주문 생성 이벤트 DLQ 저장 실패 - JSON 직렬화 오류: orderId={}, error={}",
          event.orderId(), jsonEx.getMessage(), jsonEx);
    } catch (Exception dlqEx) {
      log.error("주문 생성 이벤트 DLQ 저장 실패 - orderId={}, error={}",
          event.orderId(), dlqEx.getMessage(), dlqEx);
    }
  }
}
