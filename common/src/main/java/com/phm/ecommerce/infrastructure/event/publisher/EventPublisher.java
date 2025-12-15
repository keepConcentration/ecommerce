package com.phm.ecommerce.infrastructure.event.publisher;

public interface EventPublisher {
  void publish(Object event);
}
