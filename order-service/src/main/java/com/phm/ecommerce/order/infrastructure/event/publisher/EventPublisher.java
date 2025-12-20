package com.phm.ecommerce.order.infrastructure.event.publisher;

public interface EventPublisher {
  void publish(Object event);
}
