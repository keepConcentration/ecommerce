package com.phm.ecommerce.application.service;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.cache.EvictProductCache;
import com.phm.ecommerce.infrastructure.cache.ProductRankingService;
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
  private final ProductRankingService productRankingService;

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
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }

  public ProductIdList getPopularProductIds(int limit) {
    log.info("인기 상품 ID 조회 시작: limit={}", limit);

    List<Long> ids = productRankingService.getTopProductIds(limit);

    if (ids.isEmpty()) {
      log.warn("Redis 랭킹 데이터 없음 - DB에서 직접 조회");
      List<Product> products = productRepository.findPopularProducts(limit);
      ids = products.stream()
          .map(Product::getId)
          .toList();
      log.info("DB 직접 조회 완료: {} 개 상품 반환", ids.size());
    }

    return new ProductIdList(ids);
  }

  @EvictProductCache
  public Product saveProduct(Product product) {
    log.info("상품 저장: productId={}", product.getId());
    return productRepository.save(product);
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
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {
  }
}
