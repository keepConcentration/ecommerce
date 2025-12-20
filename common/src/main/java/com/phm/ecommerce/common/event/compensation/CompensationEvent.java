package com.phm.ecommerce.common.event.compensation;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Generic compensation event for triggering rollback of a specific step.
 * Used for reverse event chaining in Pure Choreography pattern.
 */
public record CompensationEvent(
    String orderId,
    String compensationType,  // e.g., "RESERVE_STOCK", "RESERVE_COUPON", "DEDUCT_POINTS"
    String reason,
    LocalDateTime timestamp
) implements DomainEvent {

    public static CompensationEvent create(
        String orderId,
        String compensationType,
        String reason
    ) {
        return new CompensationEvent(
            orderId,
            compensationType,
            reason,
            LocalDateTime.now()
        );
    }
}
