package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.presentation.dto.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPointsUseCase {

  private final PointRepository pointRepository;

  public PointResponse execute(Long userId) {
    Point point = pointRepository.findByUserId(userId)
        .orElseGet(() -> {
          Point newPoint = Point.create(userId);
          return pointRepository.save(newPoint);
        });

    return new PointResponse(
        point.getId(),
        point.getUserId(),
        point.getAmount(),
        point.getUpdatedAt()
    );
  }
}
