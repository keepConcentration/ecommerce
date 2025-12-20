package com.phm.ecommerce.common.event.coupon;

import com.phm.ecommerce.common.event.DomainEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when coupons are successfully reserved.
 * Triggers the next step in the saga: payment (point deduction).
 */
public record CouponReservedEvent(
    String orderId,
    Long userId,
    List<Long> reservedCouponIds,
    LocalDateTime timestamp
) implements DomainEvent {

    public static CouponReservedEvent create(
        String orderId,
        Long userId,
        List<Long> reservedCouponIds
    ) {
        return new CouponReservedEvent(
            orderId,
            userId,
            reservedCouponIds,
            LocalDateTime.now()
        );
    }
}
