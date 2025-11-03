package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.persistence.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetPointsUseCase {

  private final PointRepository pointRepository;

  public record Input(Long userId) {}

  public Output execute(Input input) {
    Point point = pointRepository.findByUserId(input.userId())
        .orElseGet(() -> {
          Point newPoint = Point.create(input.userId());
          return pointRepository.save(newPoint);
        });

    return new Output(
        point.getId(),
        point.getUserId(),
        point.getAmount(),
        point.getUpdatedAt()
    );
  }

  public record Output(
      Long pointId,
      Long userId,
      Long amount,
      LocalDateTime updatedAt) {}
}
