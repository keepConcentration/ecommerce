package com.phm.ecommerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Order 도메인 테스트")
class OrderTest {

  @Test
  @DisplayName("주문 생성 - 성공 (할인 없음)")
  void create_Success_NoDiscount() {
    // given
    Long userId = 1L;
    Long totalAmount = 50000L;
    Long discountAmount = 0L;

    // when
    Order order = Order.create(userId, totalAmount, discountAmount);

    // then
    assertAll(
        () -> assertThat(order).isNotNull(),
        () -> assertThat(order.getId()).isNull(),
        () -> assertThat(order.getUserId()).isEqualTo(userId),
        () -> assertThat(order.getTotalAmount()).isEqualTo(totalAmount),
        () -> assertThat(order.getDiscountAmount()).isEqualTo(discountAmount),
        () -> assertThat(order.getFinalAmount()).isEqualTo(50000L)
    );
  }

  @Test
  @DisplayName("주문 생성 - 성공 (할인 적용)")
  void create_Success_WithDiscount() {
    // given
    Long userId = 1L;
    Long totalAmount = 50000L;
    Long discountAmount = 5000L;

    // when
    Order order = Order.create(userId, totalAmount, discountAmount);

    // then
    assertAll(
        () -> assertThat(order.getUserId()).isEqualTo(userId),
        () -> assertThat(order.getTotalAmount()).isEqualTo(totalAmount),
        () -> assertThat(order.getDiscountAmount()).isEqualTo(discountAmount),
        () -> assertThat(order.getFinalAmount()).isEqualTo(45000L)
    );
  }

  @Test
  @DisplayName("주문 생성 - 성공 (전액 할인)")
  void create_Success_FullDiscount() {
    // given
    Long userId = 1L;
    Long totalAmount = 10000L;
    Long discountAmount = 10000L;

    // when
    Order order = Order.create(userId, totalAmount, discountAmount);

    // then
    assertThat(order.getFinalAmount()).isEqualTo(0L);
  }

  @Test
  @DisplayName("주문 생성 - 성공 (할인 금액이 총액 초과 시 총액까지만 할인)")
  void create_DiscountExceedsTotalAmount() {
    // given
    Long userId = 1L;
    Long totalAmount = 50000L;
    Long discountAmount = 60000L;

    // when
    Order order = Order.create(userId, totalAmount, discountAmount);

    // then
    assertAll(
        () -> assertThat(order.getTotalAmount()).isEqualTo(50000L),
        () -> assertThat(order.getDiscountAmount()).isEqualTo(50000L),
        () -> assertThat(order.getFinalAmount()).isEqualTo(0L)
    );
  }

  @Test
  @DisplayName("주문 생성 - 최종 금액 계산 검증")
  void create_FinalAmountCalculation() {
    // given
    Long userId = 1L;
    Long totalAmount = 100000L;
    Long discountAmount = 15000L;

    // when
    Order order = Order.create(userId, totalAmount, discountAmount);

    // then
    assertThat(order.getFinalAmount()).isEqualTo(85000L);
    assertThat(order.getFinalAmount()).isEqualTo(order.getTotalAmount() - order.getDiscountAmount());
  }
}
