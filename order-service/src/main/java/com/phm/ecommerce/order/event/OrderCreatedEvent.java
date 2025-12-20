package com.phm.ecommerce.order.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published by Order Service when an order is created.
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
) {

    public record OrderItemInfo(
        Long productId,
        Long quantity,
        Long price,
        Long userCouponId
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

    public String orderId() {
        return orderId;
    }
}
