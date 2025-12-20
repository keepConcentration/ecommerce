package com.phm.ecommerce.product.domain.product;

import com.phm.ecommerce.common.domain.product.Product;
import com.phm.ecommerce.common.domain.product.exception.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Product 도메인 테스트")
class ProductTest {

  @Test
  @DisplayName("상품 생성 - 성공")
  void create_Success() {
    // given
    String name = "테스트 상품";
    Long price = 10000L;
    Long quantity = 100L;

    // when
    Product product = Product.create(name, price, quantity);

    // then
    assertAll(
        () -> assertThat(product).isNotNull(),
        () -> assertThat(product.getId()).isNull(),
        () -> assertThat(product.getName()).isEqualTo(name),
        () -> assertThat(product.getPrice()).isEqualTo(price),
        () -> assertThat(product.getQuantity()).isEqualTo(quantity),
        () -> assertThat(product.getViewCount()).isEqualTo(0L),
        () -> assertThat(product.getSalesCount()).isEqualTo(0L)
    );
  }

  @Test
  @DisplayName("조회수 증가 - 성공")
  void increaseViewCount_Success() {
    // given
    Product product = Product.create("상품", 10000L, 100L);
    Long initialViewCount = product.getViewCount();

    // when
    product.increaseViewCount();

    // then
    assertThat(product.getViewCount()).isEqualTo(initialViewCount + 1);

    // when
    product.increaseViewCount();

    // then
    assertThat(product.getViewCount()).isEqualTo(initialViewCount + 2);
  }

  @Test
  @DisplayName("재고 감소 - 성공")
  void decreaseStock_Success() {
    // given
    Product product = Product.create("상품", 10000L, 100L);
    Long decreaseAmount = 30L;

    // when
    product.decreaseStock(decreaseAmount);

    // then
    assertThat(product.getQuantity()).isEqualTo(70L);
  }

  @Test
  @DisplayName("재고 감소 - 실패 (재고 부족)")
  void decreaseStock_Fail_InsufficientStock() {
    // given
    Product product = Product.create("상품", 10000L, 100L);
    Long decreaseAmount = 150L;

    // when & then
    assertThatThrownBy(() -> product.decreaseStock(decreaseAmount))
        .isInstanceOf(InsufficientStockException.class);

    // 재고가 변경되지 않았는지 확인
    assertThat(product.getQuantity()).isEqualTo(100L);
  }

  @Test
  @DisplayName("재고 감소 - 경계값 테스트")
  void decreaseStock_ExactAmount() {
    // given
    Product product = Product.create("상품", 10000L, 100L);
    Long decreaseAmount = 100L;

    // when
    product.decreaseStock(decreaseAmount);

    // then
    assertThat(product.getQuantity()).isEqualTo(0L);
  }

  @Test
  @DisplayName("재고 증가 - 성공")
  void increaseStock_Success() {
    // given
    Product product = Product.create("상품", 10000L, 100L);
    Long increaseAmount = 50L;

    // when
    product.increaseStock(increaseAmount);

    // then
    assertThat(product.getQuantity()).isEqualTo(150L);
  }

  @Test
  @DisplayName("재고 충분 확인")
  void hasEnoughStock() {
    // given
    Product product = Product.create("상품", 10000L, 100L);

    // when & then
    assertAll(
        () -> assertThat(product.hasEnoughStock(50L)).isTrue(),
        () -> assertThat(product.hasEnoughStock(100L)).isTrue(),
        () -> assertThat(product.hasEnoughStock(150L)).isFalse()
    );
  }

  @Test
  @DisplayName("판매량 증가 - 성공")
  void increaseSalesCount_Success() {
    // given
    Product product = Product.create("상품", 10000L, 100L);
    Long initialSalesCount = product.getSalesCount();

    // when
    product.increaseSalesCount(10L);

    // then
    assertThat(product.getSalesCount()).isEqualTo(initialSalesCount + 10L);

    // when
    product.increaseSalesCount(5L);

    // then
    assertThat(product.getSalesCount()).isEqualTo(initialSalesCount + 15L);
  }

  @Test
  @DisplayName("인기도 점수 계산")
  void calculatePopularityScore() {
    // given
    Product product = Product.create("상품", 10000L, 100L);
    product.increaseViewCount();
    product.increaseViewCount();
    product.increaseSalesCount(10L);

    // when & then
    assertThat(product.getPopularityScore()).isEqualTo(2 * 0.1 + 10 * 0.9);
  }

  @Test
  @DisplayName("인기도 점수 계산 - 조회수와 판매량이 모두 0")
  void calculatePopularityScore_ZeroValues() {
    // given
    Product product = Product.create("상품", 10000L, 100L);

    // when & then
    assertThat(product.getPopularityScore()).isEqualTo(0.0);
  }
}
