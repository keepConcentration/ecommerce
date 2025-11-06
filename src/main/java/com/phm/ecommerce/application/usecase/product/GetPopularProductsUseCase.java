package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPopularProductsUseCase {

  private final ProductRepository productRepository;

  private static final int DEFAULT_LIMIT = 5;
  private static final double DEFAULT_VIEW_WEIGHT = 0.1;
  private static final double DEFAULT_SALES_WEIGHT = 0.9;

  public List<Output> execute() {
    int limit = DEFAULT_LIMIT;
    double viewWeight = DEFAULT_VIEW_WEIGHT;
    double salesWeight = DEFAULT_SALES_WEIGHT;

    List<Product> popularProducts = productRepository.findPopularProducts(
        limit,
        viewWeight,
        salesWeight
    );

    return popularProducts.stream()
        .map(product -> new Output(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getViewCount(),
            product.getSalesCount(),
            product.calculatePopularityScore(viewWeight, salesWeight),
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
