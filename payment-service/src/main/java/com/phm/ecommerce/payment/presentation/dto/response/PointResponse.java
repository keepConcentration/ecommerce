package com.phm.ecommerce.payment.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "포인트 정보")
public record PointResponse(
    @Schema(description = "포인트 ID", example = "1")
    Long pointId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "포인트 잔액", example = "50000")
    Long amount,

    @Schema(description = "수정일시", example = "2025-01-20T10:00:00")
    LocalDateTime updatedAt) {

}
