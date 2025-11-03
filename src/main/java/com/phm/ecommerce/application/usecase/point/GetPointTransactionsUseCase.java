package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
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

  @Schema(description = "포인트 거래 내역 조회 요청")
  public record Input(
      @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId) {}

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

  @Schema(description = "포인트 거래 내역 정보")
  public record Output(
      @Schema(description = "거래 ID", example = "1")
      Long transactionId,

      @Schema(description = "포인트 ID", example = "1")
      Long pointId,

      @Schema(description = "주문 ID (충전인 경우 null)", example = "10")
      Long orderId,

      @Schema(description = "거래 금액 (양수: 충전, 음수: 사용)", example = "100000")
      Long amount,

      @Schema(description = "거래 생성일시", example = "2025-01-20T15:30:00")
      LocalDateTime createdAt) {}
}
