package com.phm.ecommerce.domain.order.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
    Long orderId,
    Long userId,
    Long finalAmount,
    LocalDateTime createdAt
) {

}
