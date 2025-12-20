package com.phm.ecommerce.product.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for publishing domain events via Outbox pattern.
 * Events are saved in the same transaction as business data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Publish a domain event via Outbox pattern.
     * Must be called within an active transaction.
     *
     * @param aggregateType The type of aggregate (e.g., "ORDER", "PRODUCT")
     * @param event The domain event to publish
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String aggregateType, DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String eventType = event.getClass().getSimpleName();

            OutboxEvent outboxEvent = OutboxEvent.create(
                aggregateType,
                event.orderId(),
                eventType,
                payload
            );

            outboxRepository.save(outboxEvent);

            log.debug("Outbox event saved - aggregateType: {}, eventType: {}, orderId: {}",
                aggregateType, eventType, event.orderId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event - type: {}, orderId: {}",
                event.getClass().getSimpleName(), event.orderId(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
