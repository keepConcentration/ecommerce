package com.phm.ecommerce.order.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 아이템 정보")
public record OrderItemResponse(
    @Schema(description = "주문 아이템 ID", example = "1")
    Long orderItemId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "노트북")
    String productName,

    @Schema(description = "수량", example = "2")
    Long quantity,

    @Schema(description = "단가", example = "1500000")
    Long price,

    @Schema(description = "총 가격", example = "3000000")
    Long totalPrice,

    @Schema(description = "할인 금액", example = "50000")
    Long discountAmount,

    @Schema(description = "최종 금액", example = "2950000")
    Long finalAmount,

    @Schema(description = "사용한 쿠폰 ID", example = "10")
    Long userCouponId) {

}
