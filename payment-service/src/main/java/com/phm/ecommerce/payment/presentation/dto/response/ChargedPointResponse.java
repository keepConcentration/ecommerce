package com.phm.ecommerce.payment.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "포인트 충전 정보")
public record ChargedPointResponse(
    @Schema(description = "포인트 ID", example = "1")
    Long pointId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "현재 포인트 잔액", example = "150000")
    Long amount,

    @Schema(description = "충전 금액", example = "100000")
    Long chargedAmount,

    @Schema(description = "포인트 거래 ID", example = "123")
    Long pointTransactionId,

    @Schema(description = "생성일시", example = "2025-01-20T15:00:00")
    LocalDateTime createdAt) {

}
