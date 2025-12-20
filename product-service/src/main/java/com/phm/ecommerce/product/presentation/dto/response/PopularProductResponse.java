package com.phm.ecommerce.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "인기 상품 정보")
public record PopularProductResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "노트북")
    String name,

    @Schema(description = "가격", example = "1500000")
    Long price,

    @Schema(description = "재고 수량", example = "50")
    Long quantity,

    @Schema(description = "조회수", example = "1523")
    Long viewCount,

    @Schema(description = "판매량", example = "342")
    Long salesCount,

    @Schema(description = "생성일시", example = "2025-01-15T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2025-01-20T15:30:00")
    LocalDateTime updatedAt) {

}
