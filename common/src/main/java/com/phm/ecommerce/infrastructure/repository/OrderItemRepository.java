package com.phm.ecommerce.infrastructure.repository;

import com.phm.ecommerce.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  List<OrderItem> findByOrderId(Long orderId);

  void deleteByOrderId(Long orderId);
}
