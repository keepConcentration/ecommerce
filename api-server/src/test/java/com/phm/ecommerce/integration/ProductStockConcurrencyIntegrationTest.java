package com.phm.ecommerce.integration;

import com.phm.ecommerce.application.usecase.order.CreateDirectOrderUseCase;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.user.User;
import com.phm.ecommerce.infrastructure.repository.PointRepository;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import com.phm.ecommerce.infrastructure.repository.UserRepository;
import com.phm.ecommerce.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DisplayName("상품 재고 동시성 테스트")
class ProductStockConcurrencyIntegrationTest extends TestContainerSupport {

  @Autowired private CreateDirectOrderUseCase createDirectOrderUseCase;

  @Autowired private ProductRepository productRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PointRepository pointRepository;

  private Long productId;
  private static final Long INITIAL_STOCK = 10L;
  private static final int CONCURRENT_USERS = 20;
  private static final Long ORDER_QUANTITY = 1L;
  private static final Long PRODUCT_PRICE = 10000L;

  @BeforeEach
  void setUp() {
    Product product = Product.create("상품", PRODUCT_PRICE, INITIAL_STOCK);
    product = productRepository.save(product);
    productId = product.getId();
  }

  @Test
  @DisplayName("재고가 10개인 상품을 20명이 동시에 1개씩 주문 시 10명만 주문에 성공한다.")
  void concurrentOrders_shouldLimitSuccessToInitialStock() throws InterruptedException {
    // given
    List<Long> userIds = new ArrayList<>();
    for (int i = 0; i < CONCURRENT_USERS; i++) {
      User user = userRepository.save(User.create());
      Point point = Point.create(user.getId());
      point.charge(100000L);
      pointRepository.save(point);
      userIds.add(user.getId());
    }

    ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_USERS);
    CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    // when
    for (Long userId : userIds) {
      executorService.submit(
          () -> {
            try {
              CreateDirectOrderUseCase.Input input =
                  new CreateDirectOrderUseCase.Input(userId, productId, ORDER_QUANTITY, null);
              createDirectOrderUseCase.execute(input);
              successCount.incrementAndGet();
            } catch (Exception e) {
              failCount.incrementAndGet();
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await();
    executorService.shutdown();

    // then
    Product finalProduct = productRepository.findByIdOrThrow(productId);
    assertAll(
        () -> assertThat(successCount.get() + failCount.get()).isEqualTo(CONCURRENT_USERS),
        () -> assertThat(successCount.get()).isEqualTo(INITIAL_STOCK.intValue()),
        () -> assertThat(finalProduct.getQuantity()).isZero(),
        () -> assertThat(successCount.get() + finalProduct.getQuantity()).isEqualTo(INITIAL_STOCK));
  }
}
