package com.phm.ecommerce.common.event.payment;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Event published when payment fails (e.g., insufficient points).
 * Triggers compensation: coupon restoration -> stock restoration.
 */
public record PaymentFailedEvent(
    String orderId,
    Long userId,
    String failureReason,
    String errorMessage,
    LocalDateTime timestamp
) implements DomainEvent {

    public static PaymentFailedEvent create(
        String orderId,
        Long userId,
        String failureReason,
        String errorMessage
    ) {
        return new PaymentFailedEvent(
            orderId,
            userId,
            failureReason,
            errorMessage,
            LocalDateTime.now()
        );
    }
}
