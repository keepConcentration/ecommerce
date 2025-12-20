package com.phm.ecommerce.order.infrastructure.event.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

  private final ApplicationEventPublisher publisher;

  @Override
  public void publish(Object event) {
    publisher.publishEvent(event);
  }
}
