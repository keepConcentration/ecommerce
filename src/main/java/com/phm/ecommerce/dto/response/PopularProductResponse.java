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
@Schema(description = "인기 상품 정보")
public class PopularProductResponse {

  @Schema(description = "상품 ID", example = "1")
  private Long productId;

  @Schema(description = "상품명", example = "노트북")
  private String name;

  @Schema(description = "가격", example = "1500000")
  private Long price;

  @Schema(description = "총 판매량", example = "150")
  private Long totalSales;
}
