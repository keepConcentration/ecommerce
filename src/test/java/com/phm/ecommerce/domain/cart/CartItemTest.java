package com.phm.ecommerce.domain.cart;

import com.phm.ecommerce.domain.cart.exception.CartItemOwnershipViolationException;
import com.phm.ecommerce.domain.cart.exception.InvalidCartQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("CartItem 도메인 테스트")
class CartItemTest {

  @Test
  @DisplayName("장바구니 아이템 생성 - 성공")
  void create_Success() {
    // given
    Long userId = 1L;
    Long productId = 100L;
    Long quantity = 5L;

    // when
    CartItem cartItem = CartItem.create(userId, productId, quantity);

    // then
    assertAll(
        () -> assertThat(cartItem).isNotNull(),
        () -> assertThat(cartItem.getId()).isNull(),
        () -> assertThat(cartItem.getUserId()).isEqualTo(userId),
        () -> assertThat(cartItem.getProductId()).isEqualTo(productId),
        () -> assertThat(cartItem.getQuantity()).isEqualTo(quantity)
    );
  }

  @Test
  @DisplayName("수량 업데이트 - 성공")
  void updateQuantity_Success() {
    // given
    CartItem cartItem = CartItem.create(1L, 100L, 5L);
    Long newQuantity = 10L;

    // when
    cartItem.updateQuantity(newQuantity);

    // then
    assertThat(cartItem.getQuantity()).isEqualTo(newQuantity);
  }

  @Test
  @DisplayName("수량 업데이트 - 실패 (유효하지 않은 수량)")
  void updateQuantity_Fail_InvalidQuantity() {
    // given
    CartItem cartItem = CartItem.create(1L, 100L, 5L);

    // when & then
    assertThatThrownBy(() -> cartItem.updateQuantity(0L))
        .isInstanceOf(InvalidCartQuantityException.class);

    assertThatThrownBy(() -> cartItem.updateQuantity(-5L))
        .isInstanceOf(InvalidCartQuantityException.class);

    assertThatThrownBy(() -> cartItem.updateQuantity(null))
        .isInstanceOf(InvalidCartQuantityException.class);
  }

  @Test
  @DisplayName("수량 증가 - 성공")
  void increaseQuantity_Success() {
    // given
    CartItem cartItem = CartItem.create(1L, 100L, 5L);
    Long additionalQuantity = 3L;

    // when
    cartItem.increaseQuantity(additionalQuantity);

    // then
    assertThat(cartItem.getQuantity()).isEqualTo(8L);
  }

  @Test
  @DisplayName("소유자 검증 - 성공 (본인)")
  void validateOwnership_Success() {
    // given
    Long userId = 1L;
    CartItem cartItem = CartItem.create(userId, 100L, 5L);

    // when & then - 예외가 발생하지 않으면 성공
    cartItem.validateOwnership(userId);
    assertThat(cartItem.belongsTo(userId)).isTrue();
  }

  @Test
  @DisplayName("소유자 검증 - 실패 (다른 사용자)")
  void validateOwnership_Fail_DifferentUser() {
    // given
    Long ownerUserId = 1L;
    Long otherUserId = 2L;
    CartItem cartItem = CartItem.create(ownerUserId, 100L, 5L);

    // when & then
    assertAll(
        () -> assertThatThrownBy(() -> cartItem.validateOwnership(otherUserId))
            .isInstanceOf(CartItemOwnershipViolationException.class),
        () -> assertThat(cartItem.belongsTo(otherUserId)).isFalse()
    );
  }
}
