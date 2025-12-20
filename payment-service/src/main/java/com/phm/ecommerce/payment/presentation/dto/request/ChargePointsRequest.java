package com.phm.ecommerce.payment.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "포인트 충전 요청")
public record ChargePointsRequest(
    @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId,

    @Schema(description = "충전 금액 (1 이상)", example = "100000", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "충전 금액은 필수입니다")
    @Min(value = 1, message = "충전 금액은 1 이상이어야 합니다")
    Long amount) {

}
