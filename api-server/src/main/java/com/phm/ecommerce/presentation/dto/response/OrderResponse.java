package com.phm.ecommerce.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 정보")
public record OrderResponse(
    @Schema(description = "주문 ID", example = "1")
    Long orderId,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "전체 주문 금액", example = "3035000")
    Long totalAmount,

    @Schema(description = "전체 할인 금액", example = "55000")
    Long discountAmount,

    @Schema(description = "최종 결제 금액", example = "2980000")
    Long finalAmount,

    @Schema(description = "주문 생성일시", example = "2025-01-20T15:30:00")
    LocalDateTime createdAt,

    @Schema(description = "주문 아이템 목록") List<OrderItemResponse> orderItems) {

}
