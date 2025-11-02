package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.point.Point;
import java.util.Optional;

public interface PointRepository {

  Point save(Point point);

  Optional<Point> findById(Long id);

  Optional<Point> findByUserId(Long userId);

  void deleteById(Long id);
}
