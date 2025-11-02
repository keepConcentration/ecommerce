package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.order.Order;
import java.util.Optional;

public interface OrderRepository {

  Order save(Order order);

  Optional<Order> findById(Long id);

  void deleteById(Long id);
}
