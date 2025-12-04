package com.phm.ecommerce.infrastructure.scheduler;

import com.phm.ecommerce.application.lock.DistributedLock;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.cache.ProductRankingService;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRankingScheduler {

  private final ProductRepository productRepository;
  private final ProductRankingService productRankingService;

  private static final int TOP_PRODUCTS_LIMIT = 100;

  @Scheduled(fixedDelay = 600000, initialDelay = 10000) // 시작후 10초 후, 10분마다 실행
  @DistributedLock(key = "'product:ranking:update'", waitTime = 10L, leaseTime = 30L)
  public void syncProductRankingToRedis() {
    try {
      log.info("총 인기 상품 랭킹 동기화 시작");

      List<Product> topProducts = productRepository.findPopularProducts(TOP_PRODUCTS_LIMIT);

      productRankingService.updateRanking(topProducts);

      log.info("총 인기 상품 랭킹 동기화 완료: {} 개 상품 동기화", topProducts.size());

    } catch (Exception e) {
      log.error("총 인기 상품 랭킹 동기화 실패", e);
    }
  }
}
