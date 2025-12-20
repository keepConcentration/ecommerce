package com.phm.ecommerce.product.infrastructure.cache;

import com.phm.ecommerce.common.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRankingService {

  private final RedisTemplate<String, Object> redisTemplate;

  public List<Long> getTopProductIds(int limit) {
    Set<Object> results = redisTemplate.opsForZSet()
        .reverseRange(RedisCacheKeys.PRODUCT_RANKING, 0, limit - 1);

    if (results == null || results.isEmpty()) {
      log.info("Redis 랭킹 데이터 없음 - 빈 목록 반환");
      return List.of();
    }

    List<Long> productIds = results.stream()
        .map(obj -> ((Number) obj).longValue())
        .toList();

    log.info("인기 상품 Top {} 조회: productIds={}", limit, productIds);
    return productIds;
  }

  public void updateRanking(List<Product> products) {
    if (products == null || products.isEmpty()) {
      log.warn("ranking update 요청이 비어있음");
      return;
    }

    log.info("인기 상품 랭킹 업데이트 시작: {} 개 상품", products.size());

    Long deletedCount = redisTemplate.delete(RedisCacheKeys.PRODUCT_RANKING) ? 1L : 0L;
    log.debug("기존 랭킹 데이터 삭제 완료: {} 개 키 삭제", deletedCount);

    redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
      for (Product product : products) {
        redisTemplate.opsForZSet().add(RedisCacheKeys.PRODUCT_RANKING, product.getId(), product.getPopularityScore());
      }
      return null;
    });

    log.info("인기 상품 랭킹 업데이트 완료: {} 개 상품", products.size());
  }
}
