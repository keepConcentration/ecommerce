package com.phm.ecommerce.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "주문 생성 요청")
public record CreateOrderRequest(
    @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId,

    @Schema(description = "장바구니 아이템별 쿠폰 매핑 (비어있으면 전체 장바구니 주문)")
    List<CartItemCouponMap> cartItemCouponMaps) {

}
