package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChargePointsUseCase {

  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;

  @Schema(description = "포인트 충전 요청")
  public record Input(
      @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId,

      @Schema(description = "충전 금액", example = "100000", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "충전 금액은 필수입니다")
      @Min(value = 1, message = "충전 금액은 1 이상이어야 합니다")
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

  @Schema(description = "충전된 포인트 정보")
  public record Output(
      @Schema(description = "포인트 ID", example = "1")
      Long pointId,

      @Schema(description = "사용자 ID", example = "1")
      Long userId,

      @Schema(description = "현재 포인트 잔액", example = "200000")
      Long amount,

      @Schema(description = "충전 금액", example = "100000")
      Long chargedAmount,

      @Schema(description = "거래 ID", example = "1")
      Long transactionId,

      @Schema(description = "거래 생성일시", example = "2025-01-20T15:30:00")
      LocalDateTime createdAt) {}
}
