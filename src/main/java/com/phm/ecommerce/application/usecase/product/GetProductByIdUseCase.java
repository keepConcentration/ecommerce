package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase {

  private final ProductRepository productRepository;

  public record Input(Long productId) {}

  public Output execute(Input input) {
    Product product = productRepository.findByIdOrThrow(input.productId());

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

  public record Output(
      Long productId,
      String name,
      Long price,
      Long quantity,
      Long viewCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {}
}
