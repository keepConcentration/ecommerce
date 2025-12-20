package com.phm.ecommerce.payment.application.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.domain.point.Point;
import com.phm.ecommerce.common.domain.point.PointTransaction;
import com.phm.ecommerce.common.domain.point.exception.InsufficientPointsException;
import com.phm.ecommerce.common.event.compensation.CompensationEvent;
import com.phm.ecommerce.common.event.coupon.CouponReservedEvent;
import com.phm.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.phm.ecommerce.common.event.payment.PaymentFailedEvent;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRecord;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRepository;
import com.phm.ecommerce.payment.infrastructure.outbox.OutboxService;
import com.phm.ecommerce.common.infrastructure.repository.PointRepository;
import com.phm.ecommerce.common.infrastructure.repository.PointTransactionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for Payment Service in Choreography Saga pattern.
 *
 * Handles:
 * - coupon.reserved: Deduct points (final step of forward flow)
 * - payment.compensation.required: Refund points (compensation)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;
  private final IdempotencyRepository idempotencyRepository;
  private final OutboxService outboxService;
  private final ObjectMapper objectMapper;

  /**
   * Listen to coupon.reserved event and deduct points.
   * This is the final step of the forward saga flow.
   * Publishes payment.completed on success or payment.failed on failure.
   */
  @KafkaListener(topics = "coupon.reserved", groupId = "payment-service-group")
  @Transactional
  public void handleCouponReserved(String message, Acknowledgment ack) {
    try {
      CouponReservedEvent event = objectMapper.readValue(message, CouponReservedEvent.class);
      String idempotencyKey = event.orderId() + ":DEDUCT_POINTS";

      log.info("CouponReserved 이벤트 수신 - orderId: {}, userId: {}",
          event.orderId(), event.userId());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 처리된 포인트 차감 - orderId: {}", event.orderId());
        // Republish success event (idempotent)
        PointTransaction existingTx = pointTransactionRepository
            .findByOrderId(Long.parseLong(event.orderId()))
            .orElseThrow(() -> new IllegalStateException("Transaction not found"));
        publishPaymentCompletedEvent(event.orderId(), event.userId(), existingTx);
        ack.acknowledge();
        return;
      }

      try {
        // Find user's point account
        Point point = pointRepository.findByUserIdOrThrow(event.userId());

        // Calculate final amount (simplified - should get from OrderCreatedEvent)
        // For now, we'll use a placeholder amount
        Long amount = 10000L;  // This should come from the original order event

        // Check if user has enough points
        if (point.getAmount() < amount) {
          log.warn("포인트 부족 - userId: {}, required: {}, available: {}",
              event.userId(), amount, point.getAmount());

          // Publish failure event - triggers compensation
          PaymentFailedEvent failedEvent = PaymentFailedEvent.create(
              event.orderId(),
              event.userId(),
              "INSUFFICIENT_POINTS",
              String.format("Required: %d, Available: %d", amount, point.getAmount())
          );
          outboxService.publish("PAYMENT", failedEvent);

          // Trigger coupon compensation
          CompensationEvent compensationEvent = CompensationEvent.create(
              event.orderId(),
              "RESERVE_COUPON",
              "Payment failed: Insufficient points"
          );
          outboxService.publish("PAYMENT", compensationEvent);

          ack.acknowledge();
          return;
        }

        // Deduct points
        point.deduct(amount);
        pointRepository.save(point);

        // Create point transaction record
        PointTransaction transaction = PointTransaction.createDeduction(
            point.getId(),
            Long.parseLong(event.orderId()),
            amount
        );
        transaction = pointTransactionRepository.save(transaction);

        log.debug("포인트 차감 완료 - orderId: {}, userId: {}, amount: {}, remainingPoints: {}",
            event.orderId(), event.userId(), amount, point.getAmount());

        // Save idempotency record
        IdempotencyRecord record = IdempotencyRecord.create(
            idempotencyKey,
            "CouponReserved",
            null,
            LocalDateTime.now().plusHours(24)
        );
        idempotencyRepository.save(record);

        // Publish PaymentCompletedEvent (final step - triggers order completion)
        PaymentCompletedEvent completedEvent = PaymentCompletedEvent.create(
            event.orderId(),
            event.userId(),
            amount,
            transaction.getId()
        );
        outboxService.publish("PAYMENT", completedEvent);

        log.info("포인트 차감 성공 - orderId: {}, userId: {}, amount: {}",
            event.orderId(), event.userId(), amount);

        ack.acknowledge();

      } catch (InsufficientPointsException e) {
        log.warn("포인트 부족으로 결제 실패 - orderId: {}", event.orderId(), e);

        // Publish failure event
        PaymentFailedEvent failedEvent = PaymentFailedEvent.create(
            event.orderId(),
            event.userId(),
            "INSUFFICIENT_POINTS",
            e.getMessage()
        );
        outboxService.publish("PAYMENT", failedEvent);

        // Trigger compensation
        CompensationEvent compensationEvent = CompensationEvent.create(
            event.orderId(),
            "RESERVE_COUPON",
            "Payment failed: " + e.getMessage()
        );
        outboxService.publish("PAYMENT", compensationEvent);

        ack.acknowledge();

      } catch (Exception e) {
        log.error("포인트 차감 처리 실패 - orderId: {}", event.orderId(), e);
        // Don't acknowledge - will be retried
        throw e;
      }

    } catch (Exception e) {
      log.error("CouponReserved 이벤트 처리 실패", e);
    }
  }

  /**
   * Listen to payment.compensation.required and refund points.
   * This is triggered when the saga needs to rollback payment.
   */
  @KafkaListener(topics = "payment.compensation.required", groupId = "payment-service-group")
  @Transactional
  public void handlePaymentCompensationRequired(String message, Acknowledgment ack) {
    try {
      CompensationEvent event = objectMapper.readValue(message, CompensationEvent.class);
      String idempotencyKey = event.orderId() + ":COMPENSATE_PAYMENT";

      log.warn("PaymentCompensation 이벤트 수신 - orderId: {}, reason: {}",
          event.orderId(), event.reason());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 보상된 포인트 - orderId: {}", event.orderId());
        // Republish next compensation event (idempotent)
        publishCouponCompensationEvent(event);
        ack.acknowledge();
        return;
      }

      // Find original transaction
      PointTransaction originalTx = pointTransactionRepository
          .findByOrderId(Long.parseLong(event.orderId()))
          .orElse(null);

      if (originalTx == null) {
        log.info("포인트 트랜잭션 없음 (결제 미실행) - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Refund points
      Point point = pointRepository.findById(originalTx.getPointId())
          .orElseThrow(() -> new IllegalStateException("Point not found: " + originalTx.getPointId()));

      point.refund(originalTx.getAmount());
      pointRepository.save(point);

      // Create refund transaction record
      PointTransaction refundTx = PointTransaction.createRefund(
          point.getId(),
          Long.parseLong(event.orderId()),
          originalTx.getAmount()
      );
      pointTransactionRepository.save(refundTx);

      log.debug("포인트 환불 완료 - orderId: {}, amount: {}, newBalance: {}",
          event.orderId(), originalTx.getAmount(), point.getAmount());

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "CompensationEvent",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      // Trigger previous step compensation: coupon
      CompensationEvent couponCompensation = CompensationEvent.create(
          event.orderId(),
          "RESERVE_COUPON",
          "Payment compensated: " + event.reason()
      );
      outboxService.publish("PAYMENT", couponCompensation);

      log.info("포인트 보상 완료 및 쿠폰 보상 트리거 - orderId: {}, refundAmount: {}",
          event.orderId(), originalTx.getAmount());

      ack.acknowledge();

    } catch (Exception e) {
      log.error("PaymentCompensation 이벤트 처리 실패", e);
    }
  }

  private void publishPaymentCompletedEvent(String orderId, Long userId, PointTransaction transaction) {
    PaymentCompletedEvent event = PaymentCompletedEvent.create(
        orderId,
        userId,
        transaction.getAmount(),
        transaction.getId()
    );
    outboxService.publish("PAYMENT", event);
  }

  private void publishCouponCompensationEvent(CompensationEvent originalEvent) {
    CompensationEvent couponCompensation = CompensationEvent.create(
        originalEvent.orderId(),
        "RESERVE_COUPON",
        "Payment compensated: " + originalEvent.reason()
    );
    outboxService.publish("PAYMENT", couponCompensation);
  }
}
