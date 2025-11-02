package com.phm.ecommerce.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 정보")
public class ProductResponse {

  @Schema(description = "상품 ID", example = "1")
  private Long productId;

  @Schema(description = "상품명", example = "노트북")
  private String name;

  @Schema(description = "가격", example = "1500000")
  private Long price;

  @Schema(description = "재고 수량", example = "50")
  private Long quantity;

  @Schema(description = "조회수", example = "1523")
  private Long viewCount;

  @Schema(description = "생성일시", example = "2025-01-15T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시", example = "2025-01-20T15:30:00")
  private LocalDateTime updatedAt;
}
