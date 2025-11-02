package com.phm.ecommerce.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "직접 주문 생성 요청")
public class DirectOrderRequest {

  @NotNull(message = "사용자 ID는 필수입니다")
  @Schema(description = "사용자 ID", example = "1", required = true)
  private Long userId;

  @NotNull(message = "상품 ID는 필수입니다")
  @Schema(description = "상품 ID", example = "1", required = true)
  private Long productId;

  @NotNull(message = "수량은 필수입니다")
  @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
  @Schema(description = "주문 수량", example = "2", required = true)
  private Long quantity;

  @Schema(description = "사용할 쿠폰 ID (선택)", example = "10")
  private Long userCouponId;
}
