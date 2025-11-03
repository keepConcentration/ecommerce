package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPointTransactionsUseCase {

  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;

  public record Input(Long userId) {}

  public List<Output> execute(Input input) {
    Point point = pointRepository.findByUserId(input.userId())
        .orElse(null);
    if (point == null) {
      return Collections.emptyList();
    }

    List<PointTransaction> transactions = pointTransactionRepository.findByPointId(point.getId());

    return transactions.stream()
        .map(transaction -> new Output(
            transaction.getId(),
            transaction.getPointId(),
            transaction.getOrderId(),
            transaction.getAmount(),
            transaction.getCreatedAt()
        ))
        .toList();
  }

  public record Output(
      Long transactionId,
      Long pointId,
      Long orderId,
      Long amount,
      LocalDateTime createdAt) {}
}
