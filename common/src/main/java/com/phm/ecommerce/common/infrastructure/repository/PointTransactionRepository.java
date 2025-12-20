package com.phm.ecommerce.common.infrastructure.repository;

import com.phm.ecommerce.common.domain.point.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

  List<PointTransaction> findByPointId(Long pointId);

  Optional<PointTransaction> findByOrderId(Long orderId);
}
