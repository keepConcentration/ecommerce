package com.phm.ecommerce.promotion.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

@Schema(description = "쿠폰 발급 요청")
public record IssueCouponRequest(
    @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId) {

}
