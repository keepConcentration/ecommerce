package com.phm.ecommerce.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 아이템 정보")
public class OrderItemResponse {

  @Schema(description = "주문 아이템 ID", example = "1")
  private Long orderItemId;

  @Schema(description = "상품 ID", example = "1")
  private Long productId;

  @Schema(description = "상품명", example = "노트북")
  private String productName;

  @Schema(description = "수량", example = "2")
  private Long quantity;

  @Schema(description = "단가", example = "1500000")
  private Long price;

  @Schema(description = "총 가격", example = "3000000")
  private Long totalPrice;

  @Schema(description = "할인 금액", example = "50000")
  private Long discountAmount;

  @Schema(description = "최종 금액", example = "2950000")
  private Long finalAmount;

  @Schema(description = "사용한 쿠폰 ID", example = "10")
  private Long userCouponId;
}
