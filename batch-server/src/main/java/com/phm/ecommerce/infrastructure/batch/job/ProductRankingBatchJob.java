package com.phm.ecommerce.infrastructure.batch.job;

import com.phm.ecommerce.application.lock.DistributedLock;
import com.phm.ecommerce.application.lock.RedisLockKeys;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.cache.ProductRankingService;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
  import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductRankingBatchJob {

  private final ProductRepository productRepository;
  private final ProductRankingService productRankingService;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  private static final int TOP_PRODUCTS_LIMIT = 100;

  @Bean
  public Job productRankingJob() {
    return new JobBuilder("productRankingJob", jobRepository)
        .start(productRankingStep())
        .build();
  }

  @Bean
  public Step productRankingStep() {
    return new StepBuilder("productRankingStep", jobRepository)
        .tasklet(productRankingTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet productRankingTasklet() {
    return (contribution, chunkContext) -> {
      syncProductRankingToRedis();
      return RepeatStatus.FINISHED;
    };
  }

  @DistributedLock(lockKeyProvider = "prepareLockKey", waitTime = 10L, leaseTime = 30L)
  public void syncProductRankingToRedis() {
    try {
      log.info("총 인기 상품 랭킹 동기화 시작");

      List<Product> topProducts = productRepository.findPopularProducts(TOP_PRODUCTS_LIMIT);

      productRankingService.updateRanking(topProducts);

      log.info("총 인기 상품 랭킹 동기화 완료: {} 개 상품 동기화", topProducts.size());

    } catch (Exception e) {
      log.error("총 인기 상품 랭킹 동기화 실패", e);
      throw e;  // Spring Batch가 실패를 인지하도록 예외 재발생
    }
  }

  private String prepareLockKey() {
    return RedisLockKeys.rankingUpdate();
  }
}
