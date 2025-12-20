package com.phm.ecommerce.common.infrastructure.repository;

import com.phm.ecommerce.common.domain.point.Point;
import com.phm.ecommerce.common.domain.point.exception.PointNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

  Optional<Point> findByUserId(Long userId);

  default Point findByUserIdOrThrow(Long userId) {
    return findByUserId(userId).orElseThrow(PointNotFoundException::new);
  }
}
