package com.phm.ecommerce.common.infrastructure.kafka;

/**
 * Centralized Kafka topic names for Choreography Saga pattern.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class
    }

    // Forward flow events (success path)
    public static final String ORDER_CREATED = "order.created";
    public static final String STOCK_RESERVED = "stock.reserved";
    public static final String COUPON_RESERVED = "coupon.reserved";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String ORDER_COMPLETED = "order.completed";

    // Failure events (trigger compensation)
    public static final String STOCK_RESERVATION_FAILED = "stock.reservation.failed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String ORDER_FAILED = "order.failed";

    // Compensation events (reverse chaining)
    public static final String STOCK_COMPENSATION_REQUIRED = "stock.compensation.required";
    public static final String COUPON_COMPENSATION_REQUIRED = "coupon.compensation.required";
    public static final String PAYMENT_COMPENSATION_REQUIRED = "payment.compensation.required";
}
