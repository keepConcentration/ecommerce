package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProductsUseCase {

  private final ProductRepository productRepository;

  public List<Output> execute() {
    List<Product> products = productRepository.findAll();

    return products.stream()
        .map(product -> new Output(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getViewCount(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        ))
        .toList();
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
