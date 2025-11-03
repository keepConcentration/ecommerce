package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import com.phm.ecommerce.presentation.dto.request.ChargePointsRequest;
import com.phm.ecommerce.presentation.dto.response.ChargedPointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChargePointsUseCase {

  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;

  public ChargedPointResponse execute(ChargePointsRequest request) {
    Point point = pointRepository.findByUserId(request.userId())
        .orElseGet(() -> {
          Point newPoint = Point.create(request.userId());
          return pointRepository.save(newPoint);
        });

    point.charge(request.amount());

    point = pointRepository.save(point);

    PointTransaction transaction = PointTransaction.createCharge(point.getId(), request.amount());
    transaction = pointTransactionRepository.save(transaction);

    return new ChargedPointResponse(
        point.getId(),
        point.getUserId(),
        point.getAmount(),
        request.amount(),
        transaction.getId(),
        transaction.getCreatedAt()
    );
  }
}
