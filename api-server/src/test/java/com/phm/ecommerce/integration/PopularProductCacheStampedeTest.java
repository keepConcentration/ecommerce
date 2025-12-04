package com.phm.ecommerce.integration;

import com.phm.ecommerce.application.service.ProductService;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.cache.ProductRankingService;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import com.phm.ecommerce.support.TestContainerSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("인기 상품 Cache Stampede 방지 테스트")
class PopularProductCacheStampedeTest extends TestContainerSupport {

  @Autowired
  private ProductService productService;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductRankingService productRankingService;

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  private static final String RANKING_KEY = "product:ranking:total";
  private static final int CONCURRENT_REQUESTS = 50;
  private List<Long> productIds;

  @BeforeEach
  void setUp() {
    productIds = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      Product product = Product.create("상품 " + i, 10000L, 100L);
      for (int j = 0; j < i * 10; j++) {
        product.increaseViewCount();
      }
      for (int j = 0; j < i * 5; j++) {
        product.increaseSalesCount(1L);
      }
      product = productRepository.save(product);
      productIds.add(product.getId());
    }

    redisTemplate.delete(RANKING_KEY);
  }

  @AfterEach
  void tearDown() {
    productRepository.deleteAll();
    redisTemplate.delete(RANKING_KEY);
  }

  @Test
  @DisplayName("Redis 캐시가 비어있을 때 동시에 50개 요청 - DB 조회는 1번만 실행")
  void cacheStampede_shouldQueryDatabaseOnlyOnce() throws InterruptedException {
    // given
    ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
    CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    // when
    for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
      executorService.submit(() -> {
        try {
          ProductService.ProductIdList result = productService.getPopularProductIds(5);
          assertThat(result.ids()).isNotEmpty();
          successCount.incrementAndGet();
        } catch (Exception e) {
          failCount.incrementAndGet();
          e.printStackTrace();
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // then
    assertThat(successCount.get()).isEqualTo(CONCURRENT_REQUESTS);
    assertThat(failCount.get()).isZero();

    List<Long> cachedIds = productRankingService.getTopProductIds(5);
    assertThat(cachedIds).hasSize(5);
    assertThat(cachedIds).containsAnyElementsOf(productIds);
  }
}
