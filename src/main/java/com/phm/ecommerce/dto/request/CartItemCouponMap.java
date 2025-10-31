package com.phm.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장바구니 아이템-쿠폰 매핑")
public class CartItemCouponMap {

  @NotNull(message = "장바구니 아이템 ID는 필수입니다")
  @Schema(description = "장바구니 아이템 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long cartItemId;

  @NotNull(message = "사용자 쿠폰 ID는 필수입니다")
  @Schema(description = "사용할 쿠폰 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long userCouponId;
}
