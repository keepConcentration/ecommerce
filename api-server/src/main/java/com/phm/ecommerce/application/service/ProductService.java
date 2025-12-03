package com.phm.ecommerce.application.service;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.InvalidDateRangeException;
import com.phm.ecommerce.infrastructure.cache.EvictProductCache;
import com.phm.ecommerce.infrastructure.cache.ProductRankingService;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import java.time.LocalDate;
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

  public ProductIdList getPopularProductIds(LocalDate date, int limit) {
    validateDateRange(date);

    log.info("인기 상품 ID 조회 시작: date={}, limit={}", date, limit);

    List<Long> ids = productRankingService.getTopProductIds(date, limit);

    if (ids.isEmpty()) {
      log.warn("Redis 랭킹 데이터 없음 - DB에서 조회하여 Redis 초기화: date={}", date);
      ids = initializeRankingFromDatabase(date, limit);
    }

    return new ProductIdList(ids);
  }

  private void validateDateRange(LocalDate date) {
    LocalDate now = LocalDate.now();
    LocalDate minDate = now.minusDays(7);
    LocalDate maxDate = now.plusDays(1);

    if (date.isBefore(minDate) || date.isAfter(maxDate)) {
      log.warn("유효하지 않은 날짜 범위 요청: date={}, 허용 범위=[{} ~ {}]", date, minDate, maxDate);
      throw new InvalidDateRangeException();
    }
  }

  private List<Long> initializeRankingFromDatabase(LocalDate date, int limit) {
    log.warn("Redis 랭킹 데이터 없음 - DB에서 직접 조회: date={}", date);
    List<Product> products = productRepository.findPopularProducts(limit);

    List<Long> ids = products.stream()
        .map(Product::getId)
        .toList();

    log.info("Redis 랭킹 초기화 완료: date={}, {} 개 상품 등록", date, ids.size());
    return ids;
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
