package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import com.phm.ecommerce.presentation.dto.response.PointTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPointTransactionsUseCase {

  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;

  public List<PointTransactionResponse> execute(Long userId) {
    Point point = pointRepository.findByUserId(userId)
        .orElse(null);
    if (point == null) {
      return Collections.emptyList();
    }

    List<PointTransaction> transactions = pointTransactionRepository.findByPointId(point.getId());

    return transactions.stream()
        .map(transaction -> new PointTransactionResponse(
            transaction.getId(),
            transaction.getPointId(),
            transaction.getOrderId(),
            transaction.getAmount(),
            transaction.getCreatedAt()
        ))
        .toList();
  }
}
