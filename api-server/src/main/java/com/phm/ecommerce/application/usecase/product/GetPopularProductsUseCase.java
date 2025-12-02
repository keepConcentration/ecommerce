package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPopularProductsUseCase {

  private final ProductService productService;

  private static final int DEFAULT_LIMIT = 5;

  public List<Output> execute() {
    return execute(DEFAULT_LIMIT);
  }

  public List<Output> execute(int limit) {
    ProductService.ProductIdList productIdList = productService.getPopularProductIds(limit);

    return productIdList.ids().stream()
        .map(productId -> {
          ProductService.ProductInfo product = productService.getProduct(productId);
          return new Output(
              product.id(),
              product.name(),
              product.price(),
              product.quantity(),
              product.viewCount(),
              product.salesCount(),
              product.popularityScore(),
              product.createdAt(),
              product.updatedAt()
          );
        })
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
