package com.phm.ecommerce.domain.coupon;

import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyUsedException;
import com.phm.ecommerce.domain.coupon.exception.CouponExpiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("UserCoupon 도메인 테스트")
class UserCouponTest {

  @Test
  @DisplayName("사용자 쿠폰 발급 - 성공")
  void issue_Success() {
    // given
    Long userId = 1L;
    Long couponId = 100L;
    Integer validDays = 7;

    // when
    UserCoupon userCoupon = UserCoupon.issue(userId, couponId, validDays);

    // then
    assertAll(
        () -> assertThat(userCoupon).isNotNull(),
        () -> assertThat(userCoupon.getId()).isNull(),
        () -> assertThat(userCoupon.getUserId()).isEqualTo(userId),
        () -> assertThat(userCoupon.getCouponId()).isEqualTo(couponId),
        () -> assertThat(userCoupon.getIssuedAt()).isNotNull(),
        () -> assertThat(userCoupon.getUsedAt()).isNull(),
        () -> assertThat(userCoupon.getExpiredAt()).isNotNull(),
        () -> assertThat(userCoupon.getExpiredAt()).isAfter(userCoupon.getIssuedAt())
    );
  }

  @Test
  @DisplayName("사용자 쿠폰 발급 - 만료일 계산 검증")
  void issue_ExpiryDateCalculation() {
    // given
    Long userId = 1L;
    Long couponId = 100L;
    Integer validDays = 30;

    // when
    UserCoupon userCoupon = UserCoupon.issue(userId, couponId, validDays);

    // then
    LocalDateTime expectedExpiredAt = userCoupon.getIssuedAt().plusDays(validDays);
    assertThat(userCoupon.getExpiredAt()).isEqualTo(expectedExpiredAt);
  }

  @Test
  @DisplayName("쿠폰 사용 가능 여부 확인 - 사용 가능")
  void isUsable_True() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);

    // when
    boolean result = userCoupon.isUsable();

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("쿠폰 사용 여부 확인 - 사용 전")
  void isUsed_False() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);

    // when
    boolean result = userCoupon.isUsed();

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("쿠폰 만료 여부 확인 - 유효한 쿠폰")
  void isExpired_False() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);

    // when
    boolean result = userCoupon.isExpired();

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("쿠폰 만료 여부 확인 - 만료된 쿠폰")
  void isExpired_True() {
    // given - 만료된 쿠폰
    LocalDateTime pastDate = LocalDateTime.now().minusDays(10);
    LocalDateTime expiredDate = LocalDateTime.now().minusDays(1);
    UserCoupon userCoupon = new UserCoupon(1L, 1L, 100L, pastDate, null, expiredDate);

    // when
    boolean result = userCoupon.isExpired();

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("쿠폰 사용 - 성공")
  void use_Success() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);
    assertThat(userCoupon.getUsedAt()).isNull();

    // when
    userCoupon.use();

    // then
    assertAll(
        () -> assertThat(userCoupon.getUsedAt()).isNotNull(),
        () -> assertThat(userCoupon.isUsed()).isTrue(),
        () -> assertThat(userCoupon.isUsable()).isFalse()
    );
  }

  @Test
  @DisplayName("쿠폰 사용 - 실패 (이미 사용된 쿠폰)")
  void use_Fail_AlreadyUsed() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);
    userCoupon.use();

    // when & then
    assertThatThrownBy(userCoupon::use)
        .isInstanceOf(CouponAlreadyUsedException.class);
  }

  @Test
  @DisplayName("쿠폰 사용 - 실패 (만료된 쿠폰)")
  void use_Fail_Expired() {
    // given - 만료된 쿠폰
    LocalDateTime pastDate = LocalDateTime.now().minusDays(10);
    LocalDateTime expiredDate = LocalDateTime.now().minusDays(1);
    UserCoupon userCoupon = new UserCoupon(1L, 1L, 100L, pastDate, null, expiredDate);

    // when & then
    assertThatThrownBy(userCoupon::use)
        .isInstanceOf(CouponExpiredException.class);
  }

  @Test
  @DisplayName("쿠폰 사용 검증 - 사용 가능한 쿠폰")
  void validateUsable_Success() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);

    // when & then
    userCoupon.validateUsable();
  }

  @Test
  @DisplayName("쿠폰 사용 검증 - 실패 (이미 사용됨)")
  void validateUsable_Fail_AlreadyUsed() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);
    userCoupon.use();

    // when & then
    assertThatThrownBy(userCoupon::validateUsable)
        .isInstanceOf(CouponAlreadyUsedException.class);
  }

  @Test
  @DisplayName("쿠폰 사용 검증 - 실패 (만료됨)")
  void validateUsable_Fail_Expired() {
    // given
    LocalDateTime pastDate = LocalDateTime.now().minusDays(10);
    LocalDateTime expiredDate = LocalDateTime.now().minusDays(1);
    UserCoupon userCoupon = new UserCoupon(1L, 1L, 100L, pastDate, null, expiredDate);

    // when & then
    assertThatThrownBy(userCoupon::validateUsable)
        .isInstanceOf(CouponExpiredException.class);
  }

  @Test
  @DisplayName("쿠폰 사용 롤백 - 성공")
  void rollbackUsage_Success() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);
    userCoupon.use(); // 사용
    assertThat(userCoupon.isUsed()).isTrue();

    // when
    userCoupon.rollbackUsage();

    // then
    assertAll(
        () -> assertThat(userCoupon.getUsedAt()).isNull(),
        () -> assertThat(userCoupon.isUsed()).isFalse(),
        () -> assertThat(userCoupon.isUsable()).isTrue()
    );
  }

  @Test
  @DisplayName("쿠폰 할인 금액 계산 - 성공")
  void calculateDiscount_Success() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);
    Coupon coupon = Coupon.create("5000원 할인", 5000L, 100L, 7);

    // when
    Long discountAmount = userCoupon.calculateDiscount(coupon);

    // then
    assertThat(discountAmount).isEqualTo(5000L);
  }

  @Test
  @DisplayName("쿠폰 할인 금액 계산 - 실패 (이미 사용된 쿠폰)")
  void calculateDiscount_Fail_AlreadyUsed() {
    // given
    UserCoupon userCoupon = UserCoupon.issue(1L, 100L, 7);
    Coupon coupon = Coupon.create("5000원 할인", 5000L, 100L, 7);
    userCoupon.use();

    // when & then
    assertThatThrownBy(() -> userCoupon.calculateDiscount(coupon))
        .isInstanceOf(CouponAlreadyUsedException.class);
  }

  @Test
  @DisplayName("쿠폰 할인 금액 계산 - 실패 (만료된 쿠폰)")
  void calculateDiscount_Fail_Expired() {
    // given
    LocalDateTime pastDate = LocalDateTime.now().minusDays(10);
    LocalDateTime expiredDate = LocalDateTime.now().minusDays(1);
    UserCoupon userCoupon = new UserCoupon(1L, 1L, 100L, pastDate, null, expiredDate);
    Coupon coupon = Coupon.create("5000원 할인", 5000L, 100L, 7);

    // when & then
    assertThatThrownBy(() -> userCoupon.calculateDiscount(coupon))
        .isInstanceOf(CouponExpiredException.class);
  }
}
