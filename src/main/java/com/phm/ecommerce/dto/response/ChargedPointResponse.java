package com.phm.ecommerce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 충전 정보")
public class ChargedPointResponse {

  @Schema(description = "포인트 ID", example = "1")
  private Long pointId;

  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "현재 포인트 잔액", example = "150000")
  private Long amount;

  @Schema(description = "충전 금액", example = "100000")
  private Long chargedAmount;

  @Schema(description = "포인트 거래 ID", example = "123")
  private Long pointTransactionId;

  @Schema(description = "생성일시", example = "2025-01-20T15:00:00")
  private LocalDateTime createdAt;
}
