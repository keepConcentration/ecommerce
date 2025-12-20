package com.phm.ecommerce.order.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 상품 추가 요청")
public record AddCartItemRequest(
    @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId,

    @Schema(description = "상품 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "상품 ID는 필수입니다")
    Long productId,

    @Schema(description = "수량 (1 이상)", example = "3", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    Long quantity) {

}
