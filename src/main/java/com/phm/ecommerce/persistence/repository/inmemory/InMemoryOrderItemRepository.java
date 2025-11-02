package com.phm.ecommerce.persistence.repository.inmemory;

import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.persistence.repository.OrderItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryOrderItemRepository implements OrderItemRepository {

  private final Map<Long, OrderItem> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public OrderItem save(OrderItem orderItem) {
    if (orderItem.getId() == null) {
      OrderItem newOrderItem =
          new OrderItem(
              idGenerator.getAndIncrement(),
              orderItem.getOrderId(),
              orderItem.getUserId(),
              orderItem.getProductId(),
              orderItem.getUserCouponId(),
              orderItem.getProductName(),
              orderItem.getQuantity(),
              orderItem.getPrice(),
              orderItem.getTotalPrice(),
              orderItem.getDiscountAmount(),
              orderItem.getFinalAmount());
      store.put(newOrderItem.getId(), newOrderItem);
      return newOrderItem;
    }
    store.put(orderItem.getId(), orderItem);
    return orderItem;
  }

  @Override
  public Optional<OrderItem> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<OrderItem> findByOrderId(Long orderId) {
    return store.values().stream().filter(item -> item.getOrderId().equals(orderId)).toList();
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
