package com.phm.ecommerce.persistence.repository.inmemory;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.persistence.repository.PointRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPointRepository implements PointRepository {

  private final Map<Long, Point> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public Point save(Point point) {
    if (point.getId() == null) {
      Point newPoint = Point.reconstruct(idGenerator.getAndIncrement(), point.getUserId(), point.getAmount());
      store.put(newPoint.getId(), newPoint);
      return newPoint;
    }
    store.put(point.getId(), point);
    return point;
  }

  @Override
  public Optional<Point> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<Point> findByUserId(Long userId) {
    return store.values().stream().filter(point -> point.getUserId().equals(userId)).findFirst();
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
