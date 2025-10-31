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
@Schema(description = "포인트 거래 내역")
public class PointTransactionResponse {

  @Schema(description = "포인트 거래 ID", example = "125")
  private Long pointTransactionId;

  @Schema(description = "포인트 ID", example = "1")
  private Long pointId;

  @Schema(description = "거래 금액 (음수: 사용, 양수: 충전)", example = "-2980000")
  private Long amount;

  @Schema(description = "생성일시", example = "2025-01-20T15:30:00")
  private LocalDateTime createdAt;
}
