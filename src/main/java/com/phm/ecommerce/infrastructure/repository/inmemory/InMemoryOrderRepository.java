package com.phm.ecommerce.infrastructure.repository.inmemory;

import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.infrastructure.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

  private final Map<Long, Order> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public Order save(Order order) {
    if (order.getId() == null) {
      Order newOrder =
          new Order(
              idGenerator.getAndIncrement(),
              order.getUserId(),
              order.getTotalAmount(),
              order.getDiscountAmount(),
              order.getFinalAmount());
      store.put(newOrder.getId(), newOrder);
      return newOrder;
    }
    store.put(order.getId(), order);
    return order;
  }

  @Override
  public Optional<Order> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
