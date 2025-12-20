package com.phm.ecommerce.product.infrastructure.outbox;

import com.phm.ecommerce.common.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Outbox pattern entity for reliable event publishing.
 * Events are saved in the same transaction as business data,
 * then asynchronously published to Kafka by OutboxEventPublisher.
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_status_created", columnList = "status, createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String aggregateType;  // "ORDER", "PRODUCT", "COUPON", "PAYMENT"

    @Column(nullable = false)
    private String aggregateId;    // orderId, productId, etc.

    @Column(nullable = false, length = 100)
    private String eventType;      // "OrderCreated", "StockReserved", etc.

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;        // JSON-serialized event data

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column
    private LocalDateTime publishedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private OutboxEvent(
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload
    ) {
        super();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public static OutboxEvent create(
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload
    ) {
        return new OutboxEvent(aggregateType, aggregateId, eventType, payload);
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public enum OutboxStatus {
        PENDING,
        PUBLISHED,
        FAILED
    }
}
