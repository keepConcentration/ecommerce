package com.phm.ecommerce.infrastructure.repository;

import com.phm.ecommerce.domain.point.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

  List<PointTransaction> findByPointId(Long pointId);
}
