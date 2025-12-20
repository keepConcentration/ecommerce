package com.phm.ecommerce.common.event.product;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Event published when stock reservation fails (e.g., insufficient stock).
 * Triggers order failure without compensation (no state changed yet).
 */
public record StockReservationFailedEvent(
    String orderId,
    String failureReason,
    String errorMessage,
    LocalDateTime timestamp
) implements DomainEvent {

    public static StockReservationFailedEvent create(
        String orderId,
        String failureReason,
        String errorMessage
    ) {
        return new StockReservationFailedEvent(
            orderId,
            failureReason,
            errorMessage,
            LocalDateTime.now()
        );
    }
}
