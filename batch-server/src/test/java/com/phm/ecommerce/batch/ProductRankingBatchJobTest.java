package com.phm.ecommerce.batch;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.cache.ProductRankingService;
import com.phm.ecommerce.infrastructure.cache.RedisCacheKeys;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import com.phm.ecommerce.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductRankingBatchJobTest extends TestContainerSupport {

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job productRankingJob;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductRankingService productRankingService;

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
    // Clear Redis ranking cache directly
    redisTemplate.delete(RedisCacheKeys.PRODUCT_RANKING);
  }

  @Test
  @DisplayName("상품 랭킹 배치가 성공적으로 실행되어야 한다")
  void productRankingBatchJobShouldComplete() throws Exception {
    // Given: 다양한 인기도를 가진 상품들 생성
    createProducts();

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(productRankingJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: Redis에 랭킹이 저장되어야 함
    List<Long> topProductIds = productRankingService.getTopProductIds(10);
    assertThat(topProductIds).isNotEmpty();
    assertThat(topProductIds.size()).isLessThanOrEqualTo(10);

    // And: 랭킹이 인기도 순으로 정렬되어야 함
    if (topProductIds.size() > 1) {
      List<Product> topProducts = productRepository.findAllById(topProductIds);
      for (int i = 0; i < topProducts.size() - 1; i++) {
        double currentScore = calculatePopularityScore(topProducts.get(i));
        double nextScore = calculatePopularityScore(topProducts.get(i + 1));
        assertThat(currentScore).isGreaterThanOrEqualTo(nextScore);
      }
    }
  }

  @Test
  @DisplayName("상품이 없는 경우에도 배치가 성공적으로 완료되어야 한다")
  void productRankingBatchJobShouldCompleteWithNoProducts() throws Exception {
    // Given: 상품이 없는 상태

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(productRankingJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: Redis에 빈 랭킹이 저장되어야 함
    List<Long> topProductIds = productRankingService.getTopProductIds(10);
    assertThat(topProductIds).isEmpty();
  }

  @Test
  @DisplayName("100개 이상의 상품이 있어도 상위 100개만 랭킹에 저장되어야 한다")
  void productRankingBatchJobShouldStoreOnlyTop100() throws Exception {
    // Given: 150개의 상품 생성
    for (int i = 0; i < 150; i++) {
      Product product = Product.create(
          "Product " + i,
          (long) (10000 + i * 100),
          100L
      );
      product.increaseViewCount();
      product.increaseSalesCount((long) (i % 10));
      productRepository.save(product);
    }

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(productRankingJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 최대 100개의 상품만 랭킹에 저장되어야 함
    List<Long> topProductIds = productRankingService.getTopProductIds(150);
    assertThat(topProductIds.size()).isLessThanOrEqualTo(100);
  }

  private void createProducts() {
    // 높은 인기도 상품
    Product highPopular = Product.create("인기 상품", 50000L, 100L);
    for (int i = 0; i < 100; i++) {
      highPopular.increaseViewCount();
    }
    highPopular.increaseSalesCount(50L);
    productRepository.save(highPopular);

    // 중간 인기도 상품
    Product mediumPopular = Product.create("보통 상품", 30000L, 100L);
    for (int i = 0; i < 50; i++) {
      mediumPopular.increaseViewCount();
    }
    mediumPopular.increaseSalesCount(20L);
    productRepository.save(mediumPopular);

    // 낮은 인기도 상품
    Product lowPopular = Product.create("비인기 상품", 20000L, 100L);
    for (int i = 0; i < 10; i++) {
      lowPopular.increaseViewCount();
    }
    lowPopular.increaseSalesCount(5L);
    productRepository.save(lowPopular);

    // 인기 없는 상품
    Product unpopular = Product.create("신상품", 15000L, 100L);
    productRepository.save(unpopular);
  }

  private double calculatePopularityScore(Product product) {
    return (product.getViewCount() * 0.1) + (product.getSalesCount() * 0.9);
  }
}
