package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProductsUseCase {

  private final ProductRepository productRepository;

  public record Input(int page, int size) {
    public Input {
      if (page < 0) {
        page = 0;
      }
      if (size <= 0 || size > 100) {
        size = 20;
      }
    }
  }

  public Page<Output> execute(Input input) {
    Pageable pageable = PageRequest.of(input.page(), input.size());
    Page<Product> productPage = productRepository.findAll(pageable);

    return productPage.map(product -> new Output(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getQuantity(),
        product.getViewCount(),
        product.getSalesCount(),
        product.getPopularityScore(),
        product.getCreatedAt(),
        product.getUpdatedAt()
    ));
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
      LocalDateTime updatedAt) {}
}
