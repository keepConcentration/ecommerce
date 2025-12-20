package com.phm.ecommerce.promotion.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "사용자 쿠폰 정보")
public record UserCouponResponse(
    @Schema(description = "사용자 쿠폰 ID", example = "10")
    Long userCouponId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "쿠폰 ID", example = "1")
    Long couponId,

    @Schema(description = "쿠폰명", example = "신규 가입 50000원 할인")
    String couponName,

    @Schema(description = "할인 금액", example = "50000")
    Long discountAmount,

    @Schema(description = "발급일시", example = "2025-01-20T10:00:00")
    LocalDateTime issuedAt,

    @Schema(description = "만료일시", example = "2025-01-27T23:59:59")
    LocalDateTime expiredAt) {

}
