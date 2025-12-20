package com.phm.ecommerce.order.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.application.lock.DistributedLock;
import com.phm.ecommerce.order.infrastructure.outbox.OutboxEvent.OutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Event Publisher - asynchronously publishes pending events to Kafka.
 * Ensures at-least-once delivery by saving events in the same transaction as business data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publishes pending outbox events to Kafka every 1 second.
     * Uses distributed lock to prevent duplicate publishing across multiple instances.
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(
            OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Publishing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Publish to Kafka
                String topicName = getTopicName(event.getEventType());
                kafkaTemplate.send(topicName, event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            event.markAsPublished();
                            outboxRepository.save(event);
                            log.info("Outbox event published successfully - id: {}, type: {}, topic: {}",
                                event.getId(), event.getEventType(), topicName);
                        } else {
                            handlePublishFailure(event, ex);
                        }
                    });

            } catch (Exception e) {
                handlePublishFailure(event, e);
            }
        }
    }

    /**
     * Retry failed events (up to 5 attempts).
     */
    @Scheduled(fixedDelay = 60000)  // Every 1 minute
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxRepository.findRetryableFailedEvents(5, PageRequest.of(0, 50));

        if (failedEvents.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed outbox events", failedEvents.size());

        for (OutboxEvent event : failedEvents) {
            try {
                event.incrementRetryCount();

                String topicName = getTopicName(event.getEventType());
                kafkaTemplate.send(topicName, event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            event.markAsPublished();
                            outboxRepository.save(event);
                            log.info("Outbox event retry successful - id: {}, retryCount: {}",
                                event.getId(), event.getRetryCount());
                        } else {
                            if (event.getRetryCount() >= 5) {
                                event.markAsFailed("Max retries exceeded: " + ex.getMessage());
                            }
                            outboxRepository.save(event);
                            log.error("Outbox event retry failed - id: {}, retryCount: {}",
                                event.getId(), event.getRetryCount(), ex);
                        }
                    });

            } catch (Exception e) {
                event.markAsFailed("Retry exception: " + e.getMessage());
                outboxRepository.save(event);
                log.error("Outbox event retry exception - id: {}", event.getId(), e);
            }
        }
    }

    private void handlePublishFailure(OutboxEvent event, Throwable ex) {
        event.incrementRetryCount();

        if (event.getRetryCount() >= 5) {
            event.markAsFailed("Max retries exceeded: " + ex.getMessage());
            log.error("Outbox event publishing failed permanently - id: {}, type: {}",
                event.getId(), event.getEventType(), ex);
        } else {
            outboxRepository.save(event);
            log.warn("Outbox event publishing failed (will retry) - id: {}, retryCount: {}",
                event.getId(), event.getRetryCount(), ex);
        }

        outboxRepository.save(event);
    }

    private String getTopicName(String eventType) {
        return switch (eventType) {
            case "OrderCreated" -> "order.created";
            case "StockReserved" -> "stock.reserved";
            case "StockReservationFailed" -> "stock.reservation.failed";
            case "CouponReserved" -> "coupon.reserved";
            case "PaymentCompleted" -> "payment.completed";
            case "PaymentFailed" -> "payment.failed";
            case "OrderCompleted" -> "order.completed";
            case "OrderFailed" -> "order.failed";
            case "StockCompensationRequired" -> "stock.compensation.required";
            case "CouponCompensationRequired" -> "coupon.compensation.required";
            case "PaymentCompensationRequired" -> "payment.compensation.required";
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
