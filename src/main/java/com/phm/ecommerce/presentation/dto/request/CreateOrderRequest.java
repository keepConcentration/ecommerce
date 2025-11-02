package com.phm.ecommerce.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 생성 요청")
public class CreateOrderRequest {

  @NotNull(message = "사용자 ID는 필수입니다")
  @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long userId;

  @Schema(description = "장바구니 아이템별 쿠폰 매핑")
  private List<CartItemCouponMap> cartItemCouponMaps;
}
