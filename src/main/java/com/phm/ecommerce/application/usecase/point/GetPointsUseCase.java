package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.persistence.repository.PointRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetPointsUseCase {

  private final PointRepository pointRepository;

  @Schema(description = "포인트 조회 요청")
  public record Input(
      @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId) {}

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

  @Schema(description = "포인트 정보")
  public record Output(
      @Schema(description = "포인트 ID", example = "1")
      Long pointId,

      @Schema(description = "사용자 ID", example = "1")
      Long userId,

      @Schema(description = "포인트 잔액", example = "150000")
      Long amount,

      @Schema(description = "최종 수정일시", example = "2025-01-20T15:30:00")
      LocalDateTime updatedAt) {}
}
