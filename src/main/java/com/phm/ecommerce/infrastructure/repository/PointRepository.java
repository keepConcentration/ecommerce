package com.phm.ecommerce.infrastructure.repository;


import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.exception.InsufficientPointsException;

import java.util.Optional;

public interface PointRepository {

  Point save(Point point);

  Optional<Point> findById(Long id);

  Optional<Point> findByUserId(Long userId);

  default Point findByUserIdOrThrow(Long userId) {
    return findByUserId(userId).orElseThrow(InsufficientPointsException::new);
  }

  void deleteById(Long id);
}
