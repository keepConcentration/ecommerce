package com.phm.ecommerce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 정보")
public class OrderResponse {

  @Schema(description = "주문 ID", example = "1")
  private Long orderId;

  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "전체 주문 금액", example = "3035000")
  private Long totalAmount;

  @Schema(description = "전체 할인 금액", example = "55000")
  private Long discountAmount;

  @Schema(description = "최종 결제 금액", example = "2980000")
  private Long finalAmount;

  @Schema(description = "주문 생성일시", example = "2025-01-20T15:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "주문 아이템 목록")
  private List<OrderItemResponse> orderItems;
}
