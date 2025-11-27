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
    productRepository.save(product);

    ProductService.ProductInfo productInfo = productService.getProduct(input.productId());
    return new Output(
        productInfo.id(),
        productInfo.name(),
        productInfo.price(),
        productInfo.quantity(),
        productInfo.viewCount(),
        productInfo.salesCount(),
        productInfo.popularityScore(),
        productInfo.createdAt(),
        productInfo.updatedAt()
    );
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
