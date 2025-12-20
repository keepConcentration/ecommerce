package com.phm.ecommerce.common.event.order;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Event published when order processing fails at any stage.
 * Marks the order as FAILED (compensation already completed).
 */
public record OrderFailedEvent(
    String orderId,
    String failureReason,
    String errorMessage,
    LocalDateTime timestamp
) implements DomainEvent {

    public static OrderFailedEvent create(
        String orderId,
        String failureReason,
        String errorMessage
    ) {
        return new OrderFailedEvent(
            orderId,
            failureReason,
            errorMessage,
            LocalDateTime.now()
        );
    }
}
