package com.phm.ecommerce.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "직접 주문 생성 요청")
public record DirectOrderRequest(
    @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId,

    @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "상품 ID는 필수입니다")
    Long productId,

    @Schema(description = "주문 수량", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    Long quantity,

    @Schema(description = "사용할 쿠폰 ID (선택)", example = "10")
    Long userCouponId) {

}
