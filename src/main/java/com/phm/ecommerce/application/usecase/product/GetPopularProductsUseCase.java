package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPopularProductsUseCase {

  private final ProductRepository productRepository;

  private static final int DEFAULT_LIMIT = 5;

  public List<Output> execute() {
    List<Product> popularProducts = productRepository.findPopularProducts(DEFAULT_LIMIT);

    return popularProducts.stream()
        .map(product -> new Output(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getViewCount(),
            product.getSalesCount(),
            product.getPopularityScore(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        ))
        .toList();
  }

  public record Output(
      Long productId,
      String name,
      Long price,
      Long quantity,
      Long viewCount,
      Long salesCount,
      Double popularityScore,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
}
