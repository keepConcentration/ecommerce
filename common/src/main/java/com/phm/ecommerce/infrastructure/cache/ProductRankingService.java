package com.phm.ecommerce.infrastructure.cache;

import com.phm.ecommerce.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRankingService {

  private static final String RANKING_KEY_PREFIX = "product:ranking:";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Duration TTL = Duration.ofDays(7);
  private final RedisTemplate<String, Object> redisTemplate;

  private String getRankingKey(LocalDate date) {
    return RANKING_KEY_PREFIX + date.format(DATE_FORMATTER);
  }

  public List<Long> getTopProductIds(LocalDate date, int limit) {
    String key = getRankingKey(date);
    Set<Object> results = redisTemplate.opsForZSet()
        .reverseRange(key, 0, limit - 1);

    if (results == null || results.isEmpty()) {
      log.info("Redis 랭킹 데이터 없음 - date={}, 빈 목록 반환", date);
      return List.of();
    }

    List<Long> productIds = results.stream()
        .map(obj -> ((Number) obj).longValue())
        .toList();

    log.info("인기 상품 Top {} 조회: date={}, productIds={}", limit, date, productIds);
    return productIds;
  }

  public void updateRanking(LocalDate date, List<Product> products) {
    if (products == null || products.isEmpty()) {
      log.warn("Bulk update 요청이 비어있음");
      return;
    }

    String key = getRankingKey(date);

    redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
      for (Product product : products) {
        redisTemplate.opsForZSet().add(key, product.getId(), product.getPopularityScore());
      }
      redisTemplate.expire(key, TTL);
      return null;
    });

    log.info("랭킹 업데이트 완료: date={}, {} 개 상품", date, products.size());
  }
}
