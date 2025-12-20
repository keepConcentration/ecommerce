package com.phm.ecommerce.common.event.order;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Event published when order is successfully completed.
 * Triggers confirmation of reservations (stock, coupon).
 */
public record OrderCompletedEvent(
    String orderId,
    Long userId,
    Long finalAmount,
    LocalDateTime completedAt,
    LocalDateTime timestamp
) implements DomainEvent {

    public static OrderCompletedEvent create(
        String orderId,
        Long userId,
        Long finalAmount,
        LocalDateTime completedAt
    ) {
        return new OrderCompletedEvent(
            orderId,
            userId,
            finalAmount,
            completedAt,
            LocalDateTime.now()
        );
    }
}
