package com.phm.ecommerce.application.event.listener;

import com.phm.ecommerce.application.service.ExternalOrderService;
import com.phm.ecommerce.domain.order.event.OrderCreatedEvent;
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

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Retryable(
      retryFor = {Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void onOrderCreated(OrderCreatedEvent event) {
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

    // TODO: 실패 이벤트 DLQ 저장 추가
  }
}
