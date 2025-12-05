package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.application.service.ProductService;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase {

  private final ProductRepository productRepository;
  private final ProductService productService;

  public record Input(Long productId) {}

  @Transactional
  public Output execute(Input input) {
    Product product = productRepository.findByIdOrThrow(input.productId());
    product.increaseViewCount();
    product = productService.saveProduct(product);

    return new Output(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getQuantity(),
        product.getViewCount(),
        product.getSalesCount(),
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
      Long salesCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {}
}
