package com.phm.ecommerce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장바구니 정보")
public class CartResponse {

  @Schema(description = "장바구니 아이템 목록")
  private List<CartItemResponse> items;
}
