package com.phm.ecommerce.common.event.payment;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Event published when payment (point deduction) is completed.
 * Triggers the final step in the saga: order completion.
 */
public record PaymentCompletedEvent(
    String orderId,
    Long userId,
    Long amount,
    Long pointTransactionId,
    LocalDateTime timestamp
) implements DomainEvent {

    public static PaymentCompletedEvent create(
        String orderId,
        Long userId,
        Long amount,
        Long pointTransactionId
    ) {
        return new PaymentCompletedEvent(
            orderId,
            userId,
            amount,
            pointTransactionId,
            LocalDateTime.now()
        );
    }
}
