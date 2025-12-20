package com.phm.ecommerce.common.event.product;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when product stock is successfully reserved.
 * Triggers the next step in the saga: coupon reservation.
 */
public record StockReservedEvent(
    String orderId,
    Long userId,
    List<StockReservation> reservations,
    LocalDateTime timestamp
) implements DomainEvent {

    public record StockReservation(
        Long productId,
        Long quantity,
        LocalDateTime reservedAt
    ) {}

    public static StockReservedEvent create(
        String orderId,
        Long userId,
        List<StockReservation> reservations
    ) {
        return new StockReservedEvent(
            orderId,
            userId,
            reservations,
            LocalDateTime.now()
        );
    }
}
