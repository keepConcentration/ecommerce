package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.order.OrderItem;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {

  OrderItem save(OrderItem orderItem);

  Optional<OrderItem> findById(Long id);

  List<OrderItem> findByOrderId(Long orderId);

  void deleteById(Long id);

  void deleteByOrderId(Long orderId);
}
