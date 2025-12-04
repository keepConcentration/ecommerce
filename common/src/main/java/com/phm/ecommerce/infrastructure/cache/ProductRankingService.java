package com.phm.ecommerce.infrastructure.cache;

import com.phm.ecommerce.domain.product.Product;
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

  private static final String RANKING_KEY = "product:ranking:total";
  private final RedisTemplate<String, Object> redisTemplate;

  public List<Long> getTopProductIds(int limit) {
    Set<Object> results = redisTemplate.opsForZSet()
        .reverseRange(RANKING_KEY, 0, limit - 1);

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

    redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
      for (Product product : products) {
        redisTemplate.opsForZSet().add(RANKING_KEY, product.getId(), product.getPopularityScore());
      }
      return null;
    });

    log.info("인기 상품 랭킹 업데이트 완료: {} 개 상품", products.size());
  }
}
