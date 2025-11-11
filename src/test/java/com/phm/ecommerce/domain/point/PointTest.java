package com.phm.ecommerce.domain.point;

import com.phm.ecommerce.domain.point.exception.InsufficientPointsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Point 도메인 테스트")
class PointTest {

  @Test
  @DisplayName("포인트 생성 - 성공")
  void create_Success() {
    // given
    Long userId = 1L;

    // when
    Point point = Point.create(userId);

    // then
    assertAll(
        () -> assertThat(point).isNotNull(),
        () -> assertThat(point.getId()).isNull(),
        () -> assertThat(point.getUserId()).isEqualTo(userId),
        () -> assertThat(point.getAmount()).isEqualTo(0L)
    );
  }

  @Test
  @DisplayName("포인트 충전 - 성공")
  void charge_Success() {
    // given
    Point point = Point.create(1L);

    // when
    point.charge(5000L);
    point.charge(3000L);
    point.charge(2000L);

    // then
    assertThat(point.getAmount()).isEqualTo(10000L);
  }

  @Test
  @DisplayName("포인트 차감 - 성공")
  void deduct_Success() {
    // given
    Point point = Point.reconstruct(1L, 1L, 10000L);
    Long deductAmount = 3000L;

    // when
    point.deduct(deductAmount);

    // then
    assertThat(point.getAmount()).isEqualTo(7000L);
  }

  @Test
  @DisplayName("포인트 차감 - 실패 (포인트 부족)")
  void deduct_Fail_InsufficientPoints() {
    // given
    Point point = Point.reconstruct(1L, 1L, 5000L);
    Long deductAmount = 7000L;

    // when & then
    assertThatThrownBy(() -> point.deduct(deductAmount))
        .isInstanceOf(InsufficientPointsException.class);

    // 포인트가 변경되지 않았는지 확인
    assertThat(point.getAmount()).isEqualTo(5000L);
  }

  @Test
  @DisplayName("포인트 차감 - 경계값")
  void deduct_ExactAmount() {
    // given
    Point point = Point.reconstruct(1L, 1L, 5000L);
    Long deductAmount = 5000L;

    // when
    point.deduct(deductAmount);

    // then
    assertThat(point.getAmount()).isEqualTo(0L);
  }

  @Test
  @DisplayName("포인트 충분 확인")
  void hasEnough() {
    // given
    Point point = Point.reconstruct(1L, 1L, 10000L);

    // when & then
    assertAll(
        () -> assertThat(point.hasEnough(5000L)).isTrue(),
        () -> assertThat(point.hasEnough(10000L)).isTrue(),
        () -> assertThat(point.hasEnough(15000L)).isFalse()
    );
  }
}
