package com.phm.ecommerce.common.event.order;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when an order is created in PENDING status.
 * This event starts the Choreography Saga flow.
 */
public record OrderCreatedEvent(
    String orderId,
    Long userId,
    List<OrderItemInfo> orderItems,
    Long totalAmount,
    Long discountAmount,
    Long finalAmount,
    LocalDateTime timestamp
) implements DomainEvent {

    public record OrderItemInfo(
        Long productId,
        Long quantity,
        Long price,
        Long userCouponId  // nullable
    ) {}

    public static OrderCreatedEvent create(
        String orderId,
        Long userId,
        List<OrderItemInfo> orderItems,
        Long totalAmount,
        Long discountAmount,
        Long finalAmount
    ) {
        return new OrderCreatedEvent(
            orderId,
            userId,
            orderItems,
            totalAmount,
            discountAmount,
            finalAmount,
            LocalDateTime.now()
        );
    }
}
