package com.phm.ecommerce.product.application.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.domain.product.Product;
import com.phm.ecommerce.common.domain.product.exception.InsufficientStockException;
import com.phm.ecommerce.common.event.compensation.CompensationEvent;
import com.phm.ecommerce.common.event.order.OrderCompletedEvent;
import com.phm.ecommerce.common.event.order.OrderCreatedEvent;
import com.phm.ecommerce.common.event.order.OrderFailedEvent;
import com.phm.ecommerce.common.event.product.StockReservationFailedEvent;
import com.phm.ecommerce.common.event.product.StockReservedEvent;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRecord;
import com.phm.ecommerce.common.infrastructure.idempotency.IdempotencyRepository;
import com.phm.ecommerce.product.infrastructure.outbox.OutboxService;
import com.phm.ecommerce.common.infrastructure.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for Product Service in Choreography Saga pattern.
 *
 * Handles:
 * - order.created: Reserve stock (decrease quantity)
 * - order.completed: Confirm reservation (increase sales count)
 * - stock.compensation.required: Restore stock (compensation)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

  private final ProductRepository productRepository;
  private final IdempotencyRepository idempotencyRepository;
  private final OutboxService outboxService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  /**
   * Listen to order.created event and reserve stock.
   * Publishes stock.reserved on success or stock.reservation.failed on failure.
   */
  @KafkaListener(topics = "order.created", groupId = "product-service-group")
  @Transactional
  public void handleOrderCreated(String message, Acknowledgment ack) {
    try {
      OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
      String idempotencyKey = event.orderId() + ":RESERVE_STOCK";

      log.info("OrderCreated 이벤트 수신 - orderId: {}, itemCount: {}",
          event.orderId(), event.orderItems().size());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 처리된 재고 예약 - orderId: {}", event.orderId());
        // Republish success event (idempotent)
        publishStockReservedEvent(event);
        ack.acknowledge();
        return;
      }

      try {
        // Reserve stock for all products
        List<StockReservedEvent.StockReservation> reservations = new ArrayList<>();
        Map<Long, Long> reservedStock = new HashMap<>();

        for (OrderCreatedEvent.OrderItemInfo item : event.orderItems()) {
          Product product = productRepository.findById(item.productId())
              .orElseThrow(() -> new IllegalStateException("Product not found: " + item.productId()));

          // Check and decrease stock
          product.decreaseStock(item.quantity());
          productRepository.save(product);

          reservedStock.put(product.getId(), item.quantity());

          // Save reservation info in Redis (TTL: 10 minutes)
          saveStockReservation(event.orderId(), product.getId(), item.quantity());

          reservations.add(new StockReservedEvent.StockReservation(
              product.getId(),
              item.quantity(),
              LocalDateTime.now()
          ));

          log.debug("재고 예약 완료 - orderId: {}, productId: {}, quantity: {}, remainingStock: {}",
              event.orderId(), product.getId(), item.quantity(), product.getQuantity());
        }

        // Save idempotency record
        IdempotencyRecord record = IdempotencyRecord.create(
            idempotencyKey,
            "OrderCreated",
            null,
            LocalDateTime.now().plusHours(24)
        );
        idempotencyRepository.save(record);

        // Publish StockReservedEvent
        StockReservedEvent reservedEvent = StockReservedEvent.create(
            event.orderId(),
            event.userId(),
            reservations
        );
        outboxService.publish("PRODUCT", reservedEvent);

        log.info("재고 예약 성공 - orderId: {}, productCount: {}",
            event.orderId(), reservations.size());

        ack.acknowledge();

      } catch (InsufficientStockException e) {
        log.warn("재고 부족으로 예약 실패 - orderId: {}", event.orderId(), e);

        // Publish failure event
        StockReservationFailedEvent failedEvent = StockReservationFailedEvent.create(
            event.orderId(),
            "INSUFFICIENT_STOCK",
            e.getMessage()
        );
        outboxService.publish("PRODUCT", failedEvent);

        ack.acknowledge();

      } catch (Exception e) {
        log.error("재고 예약 처리 실패 - orderId: {}", event.orderId(), e);
        // Don't acknowledge - will be retried
        throw e;
      }

    } catch (Exception e) {
      log.error("OrderCreated 이벤트 처리 실패", e);
    }
  }

  /**
   * Listen to order.completed event and confirm stock reservation.
   * Increase sales count and remove Redis reservation.
   */
  @KafkaListener(topics = "order.completed", groupId = "product-service-group")
  @Transactional
  public void handleOrderCompleted(String message, Acknowledgment ack) {
    try {
      OrderCompletedEvent event = objectMapper.readValue(message, OrderCompletedEvent.class);
      String idempotencyKey = event.orderId() + ":CONFIRM_STOCK";

      log.info("OrderCompleted 이벤트 수신 - orderId: {}", event.orderId());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 확정된 재고 예약 - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Get reserved stock from Redis
      Map<Long, Long> reservedStock = getStockReservations(event.orderId());

      if (reservedStock.isEmpty()) {
        log.warn("Redis 예약 정보 없음 (이미 처리됨) - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Increase sales count for each product
      for (Map.Entry<Long, Long> entry : reservedStock.entrySet()) {
        Product product = productRepository.findById(entry.getKey())
            .orElseThrow(() -> new IllegalStateException("Product not found: " + entry.getKey()));

        product.increaseSalesCount(entry.getValue());
        productRepository.save(product);

        log.debug("재고 예약 확정 - orderId: {}, productId: {}, salesCount: {}",
            event.orderId(), product.getId(), product.getSalesCount());
      }

      // Remove Redis reservation
      deleteStockReservations(event.orderId());

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "OrderCompleted",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      log.info("재고 예약 확정 완료 - orderId: {}, productCount: {}", event.orderId(), reservedStock.size());

      ack.acknowledge();

    } catch (Exception e) {
      log.error("OrderCompleted 이벤트 처리 실패", e);
    }
  }

  /**
   * Listen to stock.compensation.required and restore stock.
   * This is triggered when payment or coupon reservation fails.
   */
  @KafkaListener(topics = "stock.compensation.required", groupId = "product-service-group")
  @Transactional
  public void handleStockCompensationRequired(String message, Acknowledgment ack) {
    try {
      CompensationEvent event = objectMapper.readValue(message, CompensationEvent.class);
      String idempotencyKey = event.orderId() + ":COMPENSATE_STOCK";

      log.warn("StockCompensation 이벤트 수신 - orderId: {}, reason: {}",
          event.orderId(), event.reason());

      // Idempotency check
      if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("이미 보상된 재고 - orderId: {}", event.orderId());
        // Republish OrderFailedEvent (idempotent)
        publishOrderFailedEvent(event);
        ack.acknowledge();
        return;
      }

      // Get reserved stock from Redis
      Map<Long, Long> reservedStock = getStockReservations(event.orderId());

      if (reservedStock.isEmpty()) {
        log.warn("Redis 예약 정보 없음 (이미 처리 또는 만료) - orderId: {}", event.orderId());
        ack.acknowledge();
        return;
      }

      // Restore stock for each product
      for (Map.Entry<Long, Long> entry : reservedStock.entrySet()) {
        Product product = productRepository.findById(entry.getKey())
            .orElseThrow(() -> new IllegalStateException("Product not found: " + entry.getKey()));

        product.increaseStock(entry.getValue());
        productRepository.save(product);

        log.debug("재고 복원 완료 - orderId: {}, productId: {}, quantity: {}, newStock: {}",
            event.orderId(), product.getId(), entry.getValue(), product.getQuantity());
      }

      // Remove Redis reservation
      deleteStockReservations(event.orderId());

      // Save idempotency record
      IdempotencyRecord record = IdempotencyRecord.create(
          idempotencyKey,
          "CompensationEvent",
          null,
          LocalDateTime.now().plusHours(24)
      );
      idempotencyRepository.save(record);

      // Publish OrderFailedEvent (notify Order Service)
      OrderFailedEvent failedEvent = OrderFailedEvent.create(
          event.orderId(),
          "STOCK_COMPENSATED",
          "Stock compensation completed: " + event.reason()
      );
      outboxService.publish("PRODUCT", failedEvent);

      log.info("재고 보상 완료 - orderId: {}, productCount: {}", event.orderId(), reservedStock.size());

      ack.acknowledge();

    } catch (Exception e) {
      log.error("StockCompensation 이벤트 처리 실패", e);
    }
  }

  // Redis helpers for stock reservation tracking
  private void saveStockReservation(String orderId, Long productId, Long quantity) {
    String key = "stock:reservation:" + orderId;
    redisTemplate.opsForHash().put(key, productId.toString(), quantity.toString());
    redisTemplate.expire(key, java.time.Duration.ofMinutes(10));
  }

  private Map<Long, Long> getStockReservations(String orderId) {
    String key = "stock:reservation:" + orderId;
    Map<Object, Object> data = redisTemplate.opsForHash().entries(key);

    Map<Long, Long> result = new HashMap<>();
    data.forEach((k, v) -> result.put(Long.parseLong((String) k), Long.parseLong((String) v)));
    return result;
  }

  private void deleteStockReservations(String orderId) {
    String key = "stock:reservation:" + orderId;
    redisTemplate.delete(key);
  }

  private void publishStockReservedEvent(OrderCreatedEvent originalEvent) {
    // Reconstruct StockReservedEvent from original OrderCreatedEvent
    List<StockReservedEvent.StockReservation> reservations = originalEvent.orderItems().stream()
        .map(item -> new StockReservedEvent.StockReservation(
            item.productId(),
            item.quantity(),
            LocalDateTime.now()
        ))
        .toList();

    StockReservedEvent event = StockReservedEvent.create(
        originalEvent.orderId(),
        originalEvent.userId(),
        reservations
    );
    outboxService.publish("PRODUCT", event);
  }

  private void publishOrderFailedEvent(CompensationEvent event) {
    OrderFailedEvent failedEvent = OrderFailedEvent.create(
        event.orderId(),
        "STOCK_COMPENSATED",
        "Stock compensation completed: " + event.reason()
    );
    outboxService.publish("PRODUCT", failedEvent);
  }
}
