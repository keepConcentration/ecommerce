package com.phm.ecommerce.order.event;

import java.time.LocalDateTime;

/**
 * Event published by Order Service when order is successfully completed.
 */
public record OrderCompletedEvent(
    String orderId,
    Long userId,
    Long finalAmount,
    LocalDateTime completedAt,
    LocalDateTime timestamp
) {

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

    public String orderId() {
        return orderId;
    }
}
