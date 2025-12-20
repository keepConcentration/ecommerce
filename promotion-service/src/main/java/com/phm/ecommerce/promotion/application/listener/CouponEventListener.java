package com.phm.ecommerce.promotion.application.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.domain.coupon.UserCoupon;
import com.phm.ecommerce.common.event.compensation.CompensationEvent;
import com.phm.ecommerce.common.event.coupon.CouponReservedEvent;
import com.phm.ecommerce.common.event.order.OrderCompletedEvent;
import com.phm.ecommerce.common.event.product.StockReservedEvent;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRecord;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRepository;
import com.phm.ecommerce.promotion.infrastructure.outbox.OutboxService;
import com.phm.ecommerce.common.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.common.infrastructure.repository.UserCouponRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for Promotion Service in Choreography Saga pattern.
 *
 * Handles:
 * - stock.reserved: Reserve coupons (mark as used)
 * - order.completed: Confirm coupon usage
 * - coupon.compensation.required: Restore coupons (compensation)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CouponEventListener {

  private final UserCouponRepository userCouponRepository;
  private final CouponRepository couponRepository;
  private final IdempotencyRepository idempotencyRepository;
  private final OutboxService outboxService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  /**
   * Listen to stock.reserved event and reserve coupons.
   * Publishes coupon.reserved on success or triggers compensation on failure.
   */
  @KafkaListener(topics = "stock.reserved", groupId = "promotion-service-group")
  @Transactional
  public void handleStockReserved(String message, Acknowledgment ack) {
    try {
      StockReservedEvent event = objectMapper.readValue(message, StockReservedEvent.class);
      String idempotencyKey = event.orderId() + ":RESERVE_COUPON";

      log.info("StockReserved 이벤트 수신 - orderId: {}", event.orderId());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 처리된 쿠폰 예약 - orderId: {}", event.orderId());
        // Republish success event (idempotent)
        publishCouponReservedEvent(event.orderId(), event.userId(), new ArrayList<>());
        ack.acknowledge();
        return;
      }

      try {
        // Get user coupons from order items (from Redis or reconstruct from event)
        // For simplicity, we'll get all unused coupons for this user
        List<UserCoupon> userCoupons = userCouponRepository.findByUserIdAndUsedAtIsNull(event.userId());
        List<Long> reservedCouponIds = new ArrayList<>();

        if (!userCoupons.isEmpty()) {
          // Reserve (mark as used) up to available coupons
          for (UserCoupon userCoupon : userCoupons) {
            if (userCoupon.isUsable()) {
              userCoupon.use();
              userCouponRepository.save(userCoupon);

              // Save reservation info in Redis (TTL: 10 minutes)
              saveCouponReservation(event.orderId(), userCoupon.getId());

              reservedCouponIds.add(userCoupon.getId());

              log.debug("쿠폰 예약 완료 - orderId: {}, userCouponId: {}, couponId: {}",
                  event.orderId(), userCoupon.getId(), userCoupon.getCouponId());

              // For this simple implementation, reserve only one coupon per order
              break;
            }
          }
        }

        // Save idempotency record
        IdempotencyRecord record = IdempotencyRecord.create(
            idempotencyKey,
            "StockReserved",
            null,
            LocalDateTime.now().plusHours(24)
        );
        idempotencyRepository.save(record);

        // Publish CouponReservedEvent (even if no coupons - continues saga)
        CouponReservedEvent reservedEvent = CouponReservedEvent.create(
            event.orderId(),
            event.userId(),
            reservedCouponIds
        );
        outboxService.publish("COUPON", reservedEvent);

        log.info("쿠폰 예약 성공 - orderId: {}, reservedCouponCount: {}",
            event.orderId(), reservedCouponIds.size());

        ack.acknowledge();

      } catch (Exception e) {
        log.error("쿠폰 예약 처리 실패 - orderId: {}", event.orderId(), e);

        // Trigger compensation: restore stock
        CompensationEvent compensationEvent = CompensationEvent.create(
            event.orderId(),
            "RESERVE_COUPON",
            "Coupon reservation failed: " + e.getMessage()
        );
        outboxService.publish("COUPON", compensationEvent);

        ack.acknowledge();
      }

    } catch (Exception e) {
      log.error("StockReserved 이벤트 처리 실패", e);
    }
  }

  /**
   * Listen to order.completed event and confirm coupon usage.
   * Remove Redis reservation (coupon already marked as used).
   */
  @KafkaListener(topics = "order.completed", groupId = "promotion-service-group")
  @Transactional
  public void handleOrderCompleted(String message, Acknowledgment ack) {
    try {
      OrderCompletedEvent event = objectMapper.readValue(message, OrderCompletedEvent.class);
      String idempotencyKey = event.orderId() + ":CONFIRM_COUPON";

      log.info("OrderCompleted 이벤트 수신 - orderId: {}", event.orderId());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 확정된 쿠폰 예약 - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Get reserved coupons from Redis
      List<Long> reservedCouponIds = getCouponReservations(event.orderId());

      if (reservedCouponIds.isEmpty()) {
        log.info("예약된 쿠폰 없음 (쿠폰 미사용 주문) - orderId: {}", event.orderId());
      } else {
        log.debug("쿠폰 예약 확정 - orderId: {}, couponCount: {}",
            event.orderId(), reservedCouponIds.size());
      }

      // Remove Redis reservation
      deleteCouponReservations(event.orderId());

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "OrderCompleted",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      log.info("쿠폰 예약 확정 완료 - orderId: {}", event.orderId());

      ack.acknowledge();

    } catch (Exception e) {
      log.error("OrderCompleted 이벤트 처리 실패", e);
    }
  }

  /**
   * Listen to coupon.compensation.required and restore coupons.
   * This is triggered when payment fails.
   */
  @KafkaListener(topics = "coupon.compensation.required", groupId = "promotion-service-group")
  @Transactional
  public void handleCouponCompensationRequired(String message, Acknowledgment ack) {
    try {
      CompensationEvent event = objectMapper.readValue(message, CompensationEvent.class);
      String idempotencyKey = event.orderId() + ":COMPENSATE_COUPON";

      log.warn("CouponCompensation 이벤트 수신 - orderId: {}, reason: {}",
          event.orderId(), event.reason());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 보상된 쿠폰 - orderId: {}", event.orderId());
        // Republish next compensation event (idempotent)
        publishStockCompensationEvent(event);
        ack.acknowledge();
        return;
      }

      // Get reserved coupons from Redis
      List<Long> reservedCouponIds = getCouponReservations(event.orderId());

      if (reservedCouponIds.isEmpty()) {
        log.info("Redis 예약 정보 없음 (쿠폰 미사용 또는 이미 처리) - orderId: {}", event.orderId());
      } else {
        // Restore coupons (rollback usage)
        for (Long userCouponId : reservedCouponIds) {
          UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
              .orElseThrow(() -> new IllegalStateException("UserCoupon not found: " + userCouponId));

          userCoupon.rollbackUsage();
          userCouponRepository.save(userCoupon);

          log.debug("쿠폰 복원 완료 - orderId: {}, userCouponId: {}",
              event.orderId(), userCouponId);
        }
      }

      // Remove Redis reservation
      deleteCouponReservations(event.orderId());

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "CompensationEvent",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      // Trigger previous step compensation: stock
      CompensationEvent stockCompensation = CompensationEvent.create(
          event.orderId(),
          "RESERVE_STOCK",
          "Coupon compensated: " + event.reason()
      );
      outboxService.publish("COUPON", stockCompensation);

      log.info("쿠폰 보상 완료 및 재고 보상 트리거 - orderId: {}, couponCount: {}",
          event.orderId(), reservedCouponIds.size());

      ack.acknowledge();

    } catch (Exception e) {
      log.error("CouponCompensation 이벤트 처리 실패", e);
    }
  }

  // Redis helpers for coupon reservation tracking
  private void saveCouponReservation(String orderId, Long userCouponId) {
    String key = "coupon:reservation:" + orderId;
    redisTemplate.opsForList().rightPush(key, userCouponId.toString());
    redisTemplate.expire(key, Duration.ofMinutes(10));
  }

  private List<Long> getCouponReservations(String orderId) {
    String key = "coupon:reservation:" + orderId;
    List<Object> data = redisTemplate.opsForList().range(key, 0, -1);

    if (data == null) {
      return new ArrayList<>();
    }

    return data.stream()
        .map(obj -> Long.parseLong(obj.toString()))
        .toList();
  }

  private void deleteCouponReservations(String orderId) {
    String key = "coupon:reservation:" + orderId;
    redisTemplate.delete(key);
  }

  private void publishCouponReservedEvent(String orderId, Long userId, List<Long> couponIds) {
    CouponReservedEvent event = CouponReservedEvent.create(orderId, userId, couponIds);
    outboxService.publish("COUPON", event);
  }

  private void publishStockCompensationEvent(CompensationEvent originalEvent) {
    CompensationEvent stockCompensation = CompensationEvent.create(
        originalEvent.orderId(),
        "RESERVE_STOCK",
        "Coupon compensated: " + originalEvent.reason()
    );
    outboxService.publish("COUPON", stockCompensation);
  }
}
