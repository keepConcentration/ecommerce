package com.phm.ecommerce.persistence.repository.inmemory;

import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPointTransactionRepository implements PointTransactionRepository {

  private final Map<Long, PointTransaction> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public PointTransaction save(PointTransaction transaction) {
    if (transaction.getId() == null) {
      PointTransaction newTransaction =
          new PointTransaction(
              idGenerator.getAndIncrement(),
              transaction.getPointId(),
              transaction.getOrderId(),
              transaction.getAmount(),
              transaction.getCreatedAt());
      store.put(newTransaction.getId(), newTransaction);
      return newTransaction;
    }
    store.put(transaction.getId(), transaction);
    return transaction;
  }

  @Override
  public Optional<PointTransaction> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<PointTransaction> findByPointId(Long pointId) {
    return store.values().stream()
        .filter(transaction -> transaction.getPointId().equals(pointId))
        .toList();
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
