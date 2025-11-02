package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.point.PointTransaction;
import java.util.List;
import java.util.Optional;

public interface PointTransactionRepository {

  PointTransaction save(PointTransaction pointTransaction);

  Optional<PointTransaction> findById(Long id);

  List<PointTransaction> findByPointId(Long pointId);

  void deleteById(Long id);
}
