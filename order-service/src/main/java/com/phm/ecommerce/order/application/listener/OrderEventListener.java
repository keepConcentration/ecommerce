package com.phm.ecommerce.order.application.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.event.order.OrderCompletedEvent;
import com.phm.ecommerce.common.event.order.OrderFailedEvent;
import com.phm.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.phm.ecommerce.common.event.product.StockReservationFailedEvent;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRecord;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRepository;
import com.phm.ecommerce.order.infrastructure.outbox.OutboxService;
import com.phm.ecommerce.common.domain.order.Order;
import com.phm.ecommerce.common.infrastructure.repository.CartItemRepository;
import com.phm.ecommerce.common.infrastructure.repository.OrderRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for Order Service in Choreography Saga pattern.
 *
 * Listens to:
 * - payment.completed: Mark order as COMPLETED and clear cart
 * - stock.reservation.failed: Mark order as FAILED (no compensation needed)
 * - order.failed: Mark order as FAILED (after compensation)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

  private final OrderRepository orderRepository;
  private final CartItemRepository cartItemRepository;
  private final IdempotencyRepository idempotencyRepository;
  private final OutboxService outboxService;
  private final ObjectMapper objectMapper;

  /**
   * Handle payment completed event - final step of saga.
   * Mark order as COMPLETED and publish OrderCompletedEvent.
   */
  @KafkaListener(topics = "payment.completed", groupId = "order-service-group")
  @Transactional
  public void handlePaymentCompleted(String message, Acknowledgment ack) {
    try {
      PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
      String idempotencyKey = event.orderId() + ":COMPLETE_ORDER";

      log.info("PaymentCompleted 이벤트 수신 - orderId: {}", event.orderId());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 완료된 주문 - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Mark order as COMPLETED
      Long orderId = Long.parseLong(event.orderId());
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

      order.complete();
      orderRepository.save(order);

      // Delete cart items (order successfully completed)
      cartItemRepository.deleteByUserId(event.userId());

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "PaymentCompleted",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      // Publish OrderCompletedEvent (triggers stock/coupon confirmation)
      OrderCompletedEvent completedEvent = OrderCompletedEvent.create(
          event.orderId(),
          event.userId(),
          order.getFinalAmount(),
          order.getCreatedAt()
      );
      outboxService.publish("ORDER", completedEvent);

      log.info("주문 완료 처리 완료 - orderId: {}, userId: {}", orderId, event.userId());

      ack.acknowledge();

    } catch (Exception e) {
      log.error("PaymentCompleted 이벤트 처리 실패", e);
      // Don't acknowledge - will be retried by Kafka
    }
  }

  /**
   * Handle stock reservation failed event - early failure.
   * No compensation needed as no state was changed yet.
   */
  @KafkaListener(topics = "stock.reservation.failed", groupId = "order-service-group")
  @Transactional
  public void handleStockReservationFailed(String message, Acknowledgment ack) {
    try {
      StockReservationFailedEvent event = objectMapper.readValue(message, StockReservationFailedEvent.class);
      String idempotencyKey = event.orderId() + ":FAIL_ORDER_STOCK";

      log.warn("StockReservationFailed 이벤트 수신 - orderId: {}, reason: {}",
          event.orderId(), event.failureReason());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 실패 처리된 주문 - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Mark order as FAILED
      Long orderId = Long.parseLong(event.orderId());
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

      order.markAsFailed(event.failureReason() + ": " + event.errorMessage());
      orderRepository.save(order);

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "StockReservationFailed",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      log.info("주문 실패 처리 완료 (재고 부족) - orderId: {}", orderId);

      ack.acknowledge();

    } catch (Exception e) {
      log.error("StockReservationFailed 이벤트 처리 실패", e);
    }
  }

  /**
   * Handle order failed event - final failure after compensation.
   */
  @KafkaListener(topics = "order.failed", groupId = "order-service-group")
  @Transactional
  public void handleOrderFailed(String message, Acknowledgment ack) {
    try {
      OrderFailedEvent event = objectMapper.readValue(message, OrderFailedEvent.class);
      String idempotencyKey = event.orderId() + ":FAIL_ORDER_FINAL";

      log.warn("OrderFailed 이벤트 수신 - orderId: {}, reason: {}",
          event.orderId(), event.failureReason());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 실패 처리된 주문 - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Mark order as FAILED
      Long orderId = Long.parseLong(event.orderId());
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

      order.markAsFailed(event.failureReason() + ": " + event.errorMessage());
      orderRepository.save(order);

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "OrderFailed",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      log.info("주문 최종 실패 처리 완료 - orderId: {}, reason: {}",
          orderId, event.failureReason());

      ack.acknowledge();

    } catch (Exception e) {
      log.error("OrderFailed 이벤트 처리 실패", e);
    }
  }
}
