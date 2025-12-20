package com.phm.ecommerce.order.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 상품 삭제 요청")
public record DeleteCartItemRequest(
    @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId) {

}
