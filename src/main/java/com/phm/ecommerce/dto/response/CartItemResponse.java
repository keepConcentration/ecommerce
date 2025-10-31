package com.phm.ecommerce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장바구니 아이템 정보")
public class CartItemResponse {

  @Schema(description = "장바구니 아이템 ID", example = "1")
  private Long cartItemId;

  @Schema(description = "상품 ID", example = "1")
  private Long productId;

  @Schema(description = "상품명", example = "노트북")
  private String productName;

  @Schema(description = "가격", example = "1500000")
  private Long price;

  @Schema(description = "수량", example = "3")
  private Long quantity;
}
