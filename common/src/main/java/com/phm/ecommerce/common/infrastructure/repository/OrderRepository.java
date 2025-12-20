package com.phm.ecommerce.common.infrastructure.repository;

import com.phm.ecommerce.common.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
