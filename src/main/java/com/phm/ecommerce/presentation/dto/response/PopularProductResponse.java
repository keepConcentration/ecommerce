package com.phm.ecommerce.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인기 상품 정보")
public record PopularProductResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,
    @Schema(description = "상품명", example = "노트북")
    String name,
    @Schema(description = "가격", example = "1500000")
    Long price,
    @Schema(description = "총 판매량", example = "150")
    Long totalSales) {

}
