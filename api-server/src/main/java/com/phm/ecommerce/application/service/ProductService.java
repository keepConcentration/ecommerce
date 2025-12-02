package com.phm.ecommerce.application.service;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  @Cacheable(value = "product", key = "#productId")
  public ProductInfo getProduct(Long productId) {
    log.info("DB에서 상품 조회: productId={}", productId);
    Product product = productRepository.findByIdOrThrow(productId);

    return new ProductInfo(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getQuantity(),
        product.getViewCount(),
        product.getSalesCount(),
        product.getPopularityScore(),
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }

  @Cacheable(value = "popularProductIds", key = "'top' + #limit")
  public ProductIdList getPopularProductIds(int limit) {
    log.info("DB에서 인기 상품 ID 조회: limit={}", limit);
    List<Long> ids = productRepository.findPopularProducts(limit)
        .stream()
        .map(Product::getId)
        .toList();
    return new ProductIdList(ids);
  }

  public record ProductIdList(List<Long> ids) {
  }

  public record ProductInfo(
      Long id,
      String name,
      Long price,
      Long quantity,
      Long viewCount,
      Long salesCount,
      Double popularityScore,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {
  }
}
