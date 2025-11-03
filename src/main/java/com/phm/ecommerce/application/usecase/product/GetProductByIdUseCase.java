package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase {

  private final ProductRepository productRepository;

  @Schema(description = "상품 조회 요청")
  public record Input(
      @Schema(description = "상품 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "상품 ID는 필수입니다")
      Long productId) {}

  public Output execute(Input input) {
    Product product = productRepository.findById(input.productId())
        .orElseThrow(ProductNotFoundException::new);

    product.increaseViewCount();
    product = productRepository.save(product);

    return new Output(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getQuantity(),
        product.getViewCount(),
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }

  @Schema(description = "상품 정보")
  public record Output(
      @Schema(description = "상품 ID", example = "1")
      Long productId,

      @Schema(description = "상품명", example = "노트북")
      String name,

      @Schema(description = "가격", example = "1500000")
      Long price,

      @Schema(description = "재고 수량", example = "10")
      Long quantity,

      @Schema(description = "조회 수", example = "123")
      Long viewCount,

      @Schema(description = "생성일시", example = "2025-01-01T10:00:00")
      LocalDateTime createdAt,

      @Schema(description = "수정일시", example = "2025-01-20T15:30:00")
      LocalDateTime updatedAt) {}
}
