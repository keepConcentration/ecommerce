package com.phm.ecommerce.product.application.usecase.product;

import com.phm.ecommerce.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPopularProductsUseCase {

  private final ProductService productService;

  private static final int DEFAULT_LIMIT = 5;

  public record Input(Integer limit) {
    public Input {
      if (limit == null) {
        limit = DEFAULT_LIMIT;
      }
    }
  }

  public List<Output> execute(Input input) {
    ProductService.ProductIdList productIdList = productService.getPopularProductIds(input.limit());

    List<ProductService.ProductInfo> products = productService.getProductsByIds(productIdList.ids());

    return products.stream()
        .map(product -> new Output(
            product.id(),
            product.name(),
            product.price(),
            product.quantity(),
            product.viewCount(),
            product.salesCount(),
            product.createdAt(),
            product.updatedAt()
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
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
}
