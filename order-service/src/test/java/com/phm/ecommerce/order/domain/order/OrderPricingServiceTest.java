package com.phm.ecommerce.order.domain.order;

import com.phm.ecommerce.common.domain.order.OrderPricingService;
import com.phm.ecommerce.common.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderPricingService 도메인 서비스 테스트")
class OrderPricingServiceTest {

  private OrderPricingService orderPricingService;

  @BeforeEach
  void setUp() {
    orderPricingService = new OrderPricingService();
  }

  @Test
  @DisplayName("상품 총액 계산 - 성공")
  void calculateItemTotal_Success() {
    // given
    Product product = Product.create("테스트 상품", 10000L, 100L);
    Long quantity = 3L;

    // when
    Long totalAmount = orderPricingService.calculateItemTotal(product, quantity);

    // then
    assertThat(totalAmount).isEqualTo(30000L);
  }

  @Test
  @DisplayName("최종 금액 계산 - 성공 (할인 적용)")
  void calculateFinalAmount_Success() {
    // given
    Long totalAmount = 50000L;
    Long discountAmount = 5000L;

    // when
    Long finalAmount = orderPricingService.calculateFinalAmount(totalAmount, discountAmount);

    // then
    assertThat(finalAmount).isEqualTo(45000L);
  }

  @Test
  @DisplayName("최종 금액 계산 - 성공 (할인 없음)")
  void calculateFinalAmount_NoDiscount() {
    // given
    Long totalAmount = 50000L;
    Long discountAmount = 0L;

    // when
    Long finalAmount = orderPricingService.calculateFinalAmount(totalAmount, discountAmount);

    // then
    assertThat(finalAmount).isEqualTo(50000L);
  }

  @Test
  @DisplayName("최종 금액 계산 - 성공 (전액 할인)")
  void calculateFinalAmount_FullDiscount() {
    // given
    Long totalAmount = 10000L;
    Long discountAmount = 10000L;

    // when
    Long finalAmount = orderPricingService.calculateFinalAmount(totalAmount, discountAmount);

    // then
    assertThat(finalAmount).isEqualTo(0L);
  }

  @Test
  @DisplayName("최종 금액 계산 - 성공 (할인 금액이 총액 초과 시 0원)")
  void calculateFinalAmount_DiscountExceedsTotal() {
    // given
    Long totalAmount = 50000L;
    Long discountAmount = 60000L;

    // when
    Long finalAmount = orderPricingService.calculateFinalAmount(totalAmount, discountAmount);

    // then
    assertThat(finalAmount).isEqualTo(0L);
  }

  @Test
  @DisplayName("금액 합계 계산 - 성공")
  void sumAmounts_Success() {
    // given
    Long amount1 = 10000L;
    Long amount2 = 20000L;
    Long amount3 = 30000L;

    // when
    Long total = orderPricingService.sumAmounts(amount1, amount2, amount3);

    // then
    assertThat(total).isEqualTo(60000L);
  }

  @Test
  @DisplayName("금액 합계 계산 - 금액 없음")
  void sumAmounts_NoAmounts() {
    // when
    Long total = orderPricingService.sumAmounts();

    // then
    assertThat(total).isEqualTo(0L);
  }
}
