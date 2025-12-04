package com.phm.ecommerce.infrastructure.cache;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

  private final ProductRepository productRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final String PRODUCT_CACHE_KEY_PREFIX = "product:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(30);

  public List<ProductInfo> getProductsByIds(List<Long> productIds) {
    if (productIds.isEmpty()) {
      return List.of();
    }

    List<Long> cacheMisses = new ArrayList<>();
    List<ProductInfo> cachedProducts = new ArrayList<>();

    for (Long productId : productIds) {
      String cacheKey = PRODUCT_CACHE_KEY_PREFIX + productId;
      ProductInfo cached = (ProductInfo) redisTemplate.opsForValue().get(cacheKey);

      if (cached != null) {
        cachedProducts.add(cached);
      } else {
        cacheMisses.add(productId);
      }
    }

    log.debug("상품 조회 - 전체: {}, 캐시 히트: {}, 캐시 미스: {}",
        productIds.size(), productIds.size() - cacheMisses.size(), cacheMisses.size());

    List<ProductInfo> dbProducts = List.of();
    if (!cacheMisses.isEmpty()) {
      log.debug("DB에서 상품 조회: {} 개", cacheMisses.size());
      List<Product> products = productRepository.findAllByIds(cacheMisses);

      dbProducts = products.stream()
          .map(product -> {
            ProductInfo info = toProductInfo(product);

            String cacheKey = PRODUCT_CACHE_KEY_PREFIX + product.getId();
            redisTemplate.opsForValue().set(cacheKey, info, CACHE_TTL);

            return info;
          })
          .toList();
    }

    var allProducts = new ArrayList<>(cachedProducts);
    allProducts.addAll(dbProducts);

    // 요청 순서대로 정렬
    var productMap = allProducts.stream()
        .collect(Collectors.toMap(ProductInfo::id, p -> p));

    return productIds.stream()
        .map(productMap::get)
        .filter(Objects::nonNull)
        .toList();
  }

  public void evictProductCache(Long productId) {
    String cacheKey = PRODUCT_CACHE_KEY_PREFIX + productId;
    Boolean deleted = redisTemplate.delete(cacheKey);
    log.debug("상품 캐시 삭제: productId={}, deleted={}", productId, deleted);
  }

  private ProductInfo toProductInfo(Product product) {
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
