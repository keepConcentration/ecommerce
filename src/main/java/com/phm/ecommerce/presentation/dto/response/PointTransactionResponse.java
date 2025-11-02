package com.phm.ecommerce.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "포인트 거래 내역")
public record PointTransactionResponse(
    @Schema(description = "포인트 거래 ID", example = "125")
    Long pointTransactionId,

    @Schema(description = "포인트 ID", example = "1")
    Long pointId,

    @Schema(description = "주문 ID (주문 결제인 경우)", example = "1", nullable = true)
    Long orderId,

    @Schema(description = "거래 금액 (음수: 사용, 양수: 충전)", example = "-2980000")
    Long amount,

    @Schema(description = "생성일시", example = "2025-01-20T15:30:00")
    LocalDateTime createdAt) {

}
