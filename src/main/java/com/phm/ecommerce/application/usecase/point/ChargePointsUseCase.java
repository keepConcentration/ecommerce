package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChargePointsUseCase {

  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;

  public record Input(
      Long userId,
      Long amount) {}

  public Output execute(Input request) {
    Point point = pointRepository.findByUserId(request.userId())
        .orElseGet(() -> {
          Point newPoint = Point.create(request.userId());
          return pointRepository.save(newPoint);
        });

    point.charge(request.amount());

    point = pointRepository.save(point);

    PointTransaction transaction = PointTransaction.createCharge(point.getId(), request.amount());
    transaction = pointTransactionRepository.save(transaction);

    return new Output(
        point.getId(),
        point.getUserId(),
        point.getAmount(),
        request.amount(),
        transaction.getId(),
        transaction.getCreatedAt()
    );
  }

  public record Output(
      Long pointId,
      Long userId,
      Long amount,
      Long chargedAmount,
      Long transactionId,
      LocalDateTime createdAt) {}
}
