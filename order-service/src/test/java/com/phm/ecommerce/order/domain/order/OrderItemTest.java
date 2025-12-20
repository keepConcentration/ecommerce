package com.phm.ecommerce.order.domain.order;

import com.phm.ecommerce.common.domain.order.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItem 도메인 테스트")
class OrderItemTest {

  @Test
  @DisplayName("주문 아이템 생성 - 성공 (할인 없음)")
  void create_Success_NoDiscount() {
    // given
    Long orderId = 1L;
    Long userId = 1L;
    Long productId = 100L;
    String productName = "테스트 상품";
    Long quantity = 3L;
    Long price = 10000L;
    Long discountAmount = 0L;
    Long userCouponId = null;

    // when
    OrderItem orderItem = OrderItem.create(
        orderId, userId, productId, productName, quantity, price, discountAmount, userCouponId);

    // then
    assertThat(orderItem).isNotNull();
    assertThat(orderItem.getId()).isNull();
    assertThat(orderItem.getOrderId()).isEqualTo(orderId);
    assertThat(orderItem.getUserId()).isEqualTo(userId);
    assertThat(orderItem.getProductId()).isEqualTo(productId);
    assertThat(orderItem.getProductName()).isEqualTo(productName);
    assertThat(orderItem.getQuantity()).isEqualTo(quantity);
    assertThat(orderItem.getPrice()).isEqualTo(price);
    assertThat(orderItem.getTotalPrice()).isEqualTo(30000L); // 10000 * 3
    assertThat(orderItem.getDiscountAmount()).isEqualTo(discountAmount);
    assertThat(orderItem.getFinalAmount()).isEqualTo(30000L);
    assertThat(orderItem.getUserCouponId()).isNull();
  }

  @Test
  @DisplayName("주문 아이템 생성 - 성공 (할인 적용)")
  void create_Success_WithDiscount() {
    // given
    Long orderId = 1L;
    Long userId = 1L;
    Long productId = 100L;
    String productName = "테스트 상품";
    Long quantity = 2L;
    Long price = 20000L;
    Long discountAmount = 5000L;
    Long userCouponId = 10L;

    // when
    OrderItem orderItem = OrderItem.create(
        orderId, userId, productId, productName, quantity, price, discountAmount, userCouponId);

    // then
    assertThat(orderItem.getTotalPrice()).isEqualTo(40000L); // 20000 * 2
    assertThat(orderItem.getDiscountAmount()).isEqualTo(5000L);
    assertThat(orderItem.getFinalAmount()).isEqualTo(35000L); // 40000 - 5000
    assertThat(orderItem.getUserCouponId()).isEqualTo(userCouponId);
  }

  @Test
  @DisplayName("주문 아이템 생성 - 성공 (할인 금액이 총액 초과 시 총액까지만 할인)")
  void create_DiscountExceedsTotalAmount() {
    // given
    Long orderId = 1L;
    Long userId = 1L;
    Long productId = 100L;
    String productName = "테스트 상품";
    Long quantity = 2L;
    Long price = 10000L;
    Long discountAmount = 25000L; // totalPrice(20000)보다 큼
    Long userCouponId = 10L;

    // when
    OrderItem orderItem = OrderItem.create(
        orderId, userId, productId, productName, quantity, price, discountAmount, userCouponId);

    // then
    assertThat(orderItem.getTotalPrice()).isEqualTo(20000L); // 10000 * 2
    assertThat(orderItem.getDiscountAmount()).isEqualTo(20000L); // 실제 할인된 금액
    assertThat(orderItem.getFinalAmount()).isEqualTo(0L); // 20000 - 20000
    assertThat(orderItem.getUserCouponId()).isEqualTo(userCouponId);
  }

  @Test
  @DisplayName("주문 아이템 생성 - 총액 계산 검증")
  void create_TotalPriceCalculation() {
    // given
    Long quantity = 5L;
    Long price = 7000L;

    // when
    OrderItem orderItem = OrderItem.create(
        1L, 1L, 100L, "상품", quantity, price, 0L, null);

    // then
    assertThat(orderItem.getTotalPrice()).isEqualTo(35000L);
    assertThat(orderItem.getTotalPrice()).isEqualTo(price * quantity);
  }

  @Test
  @DisplayName("주문 아이템 생성 - 최종 금액 계산 검증")
  void create_FinalAmountCalculation() {
    // given
    Long quantity = 4L;
    Long price = 15000L;
    Long discountAmount = 10000L;

    // when
    OrderItem orderItem = OrderItem.create(
        1L, 1L, 100L, "상품", quantity, price, discountAmount, 10L);

    // then
    Long expectedTotalPrice = 60000L; // 15000 * 4
    Long expectedFinalAmount = 50000L; // 60000 - 10000

    assertThat(orderItem.getTotalPrice()).isEqualTo(expectedTotalPrice);
    assertThat(orderItem.getFinalAmount()).isEqualTo(expectedFinalAmount);
    assertThat(orderItem.getFinalAmount()).isEqualTo(orderItem.getTotalPrice() - orderItem.getDiscountAmount());
  }

  @Test
  @DisplayName("주문 아이템 생성 - 전액 할인")
  void create_FullDiscount() {
    // given
    Long quantity = 2L;
    Long price = 5000L;
    Long discountAmount = 10000L;

    // when
    OrderItem orderItem = OrderItem.create(
        1L, 1L, 100L, "상품", quantity, price, discountAmount, 10L);

    // then
    assertThat(orderItem.getTotalPrice()).isEqualTo(10000L);
    assertThat(orderItem.getFinalAmount()).isEqualTo(0L);
  }

  @Test
  @DisplayName("주문 아이템 생성 - 수량 1개")
  void create_SingleQuantity() {
    // given
    Long quantity = 1L;
    Long price = 50000L;
    Long discountAmount = 5000L;

    // when
    OrderItem orderItem = OrderItem.create(
        1L, 1L, 100L, "상품", quantity, price, discountAmount, null);

    // then
    assertThat(orderItem.getQuantity()).isEqualTo(1L);
    assertThat(orderItem.getTotalPrice()).isEqualTo(50000L);
    assertThat(orderItem.getFinalAmount()).isEqualTo(45000L);
  }

  @Test
  @DisplayName("주문 아이템 생성 - 대량 수량")
  void create_LargeQuantity() {
    // given
    Long quantity = 100L;
    Long price = 1000L;
    Long discountAmount = 10000L;

    // when
    OrderItem orderItem = OrderItem.create(
        1L, 1L, 100L, "상품", quantity, price, discountAmount, null);

    // then
    assertThat(orderItem.getTotalPrice()).isEqualTo(100000L);
    assertThat(orderItem.getFinalAmount()).isEqualTo(90000L);
  }

  @Test
  @DisplayName("주문 아이템 생성 - 다양한 할인 시나리오")
  void create_VariousDiscountScenarios() {
    // 10% 할인
    OrderItem item1 = OrderItem.create(1L, 1L, 100L, "상품1", 2L, 10000L, 2000L, null);
    assertThat(item1.getTotalPrice()).isEqualTo(20000L);
    assertThat(item1.getFinalAmount()).isEqualTo(18000L);

    // 50% 할인
    OrderItem item2 = OrderItem.create(1L, 1L, 100L, "상품2", 1L, 30000L, 15000L, 10L);
    assertThat(item2.getTotalPrice()).isEqualTo(30000L);
    assertThat(item2.getFinalAmount()).isEqualTo(15000L);

    // 할인 없음
    OrderItem item3 = OrderItem.create(1L, 1L, 100L, "상품3", 3L, 5000L, 0L, null);
    assertThat(item3.getTotalPrice()).isEqualTo(15000L);
    assertThat(item3.getFinalAmount()).isEqualTo(15000L);
  }

  @Test
  @DisplayName("주문 아이템 생성 - 쿠폰 ID가 있는 경우와 없는 경우")
  void create_WithAndWithoutCoupon() {
    // 쿠폰 없음
    OrderItem itemWithoutCoupon = OrderItem.create(
        1L, 1L, 100L, "상품", 1L, 10000L, 0L, null);
    assertThat(itemWithoutCoupon.getUserCouponId()).isNull();
    assertThat(itemWithoutCoupon.getDiscountAmount()).isEqualTo(0L);

    // 쿠폰 있음
    OrderItem itemWithCoupon = OrderItem.create(
        1L, 1L, 100L, "상품", 1L, 10000L, 2000L, 5L);
    assertThat(itemWithCoupon.getUserCouponId()).isEqualTo(5L);
    assertThat(itemWithCoupon.getDiscountAmount()).isEqualTo(2000L);
  }
}
