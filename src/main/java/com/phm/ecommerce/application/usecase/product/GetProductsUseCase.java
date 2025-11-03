package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.ProductRepository;
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

  public record Output(
      Long productId,
      String name,
      Long price,
      Long quantity,
      Long viewCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {}
}
