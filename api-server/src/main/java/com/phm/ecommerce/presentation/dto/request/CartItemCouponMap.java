package com.phm.ecommerce.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 아이템-쿠폰 매핑")
public record CartItemCouponMap(

    @Schema(description = "장바구니 아이템 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "장바구니 아이템 ID는 필수입니다")
    Long cartItemId,

    @Schema(description = "사용할 쿠폰 ID (쿠폰 미사용 시 null)", example = "10")
    Long userCouponId) {

}
