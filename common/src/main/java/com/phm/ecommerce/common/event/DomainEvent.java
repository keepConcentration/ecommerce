package com.phm.ecommerce.common.event;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events in the Choreography Saga pattern.
 * All events must have an orderId for correlation and timestamp for ordering.
 */
public interface DomainEvent {

    String orderId();

    LocalDateTime timestamp();
}
