package com.phm.ecommerce.application.service;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.cache.ProductCacheService;
import com.phm.ecommerce.infrastructure.cache.ProductRankingService;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductRankingService productRankingService;
  private final ProductCacheService productCacheService;

  public List<ProductInfo> getProductsByIds(List<Long> productIds) {
    log.info("상품 조회 요청: {} 개", productIds.size());

    List<ProductCacheService.ProductInfo> cachedInfos = productCacheService.getProductsByIds(productIds);

    return cachedInfos.stream()
        .map(info -> new ProductInfo(
            info.id(),
            info.name(),
            info.price(),
            info.quantity(),
            info.viewCount(),
            info.salesCount(),
            info.createdAt(),
            info.updatedAt()
        ))
        .toList();
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

  public Product saveProduct(Product product) {
    log.info("상품 저장: productId={}", product.getId());
    Product saved = productRepository.save(product);

    if (saved.getId() != null) {
      productCacheService.evictProductCache(saved.getId());
    }

    return saved;
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
