package com.phm.ecommerce.common.infrastructure.idempotency;

import com.phm.ecommerce.common.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Idempotency record for preventing duplicate event processing.
 * Each event processing is tracked by a unique idempotency key.
 */
@Entity
@Table(name = "idempotency_records", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true),
    @Index(name = "idx_idempotency_expires", columnList = "expiresAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord extends BaseEntity {

    @Column(unique = true, nullable = false, length = 255)
    private String idempotencyKey;  // "ORDER:12345:RESERVE_STOCK"

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String response;  // JSON-serialized response for retry

    @Column(nullable = false)
    private LocalDateTime processedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;  // TTL for cleanup

    private IdempotencyRecord(
        String idempotencyKey,
        String eventType,
        String response,
        LocalDateTime expiresAt
    ) {
        super();
        this.idempotencyKey = idempotencyKey;
        this.eventType = eventType;
        this.response = response;
        this.processedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public static IdempotencyRecord create(
        String idempotencyKey,
        String eventType,
        String response,
        LocalDateTime expiresAt
    ) {
        return new IdempotencyRecord(idempotencyKey, eventType, response, expiresAt);
    }
}
