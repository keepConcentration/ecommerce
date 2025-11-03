package com.phm.ecommerce.application.usecase.coupon;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserCouponsUseCase {

  private final UserCouponRepository userCouponRepository;
  private final CouponRepository couponRepository;

  @Schema(description = "사용자 쿠폰 목록 조회 요청")
  public record Input(
      @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId) {}

  public List<Output> execute(Input input) {
    List<UserCoupon> userCoupons = userCouponRepository.findByUserId(input.userId());

    List<UserCoupon> usableCoupons = userCoupons.stream()
        .filter(UserCoupon::isUsable)
        .toList();

    List<Long> couponIds = usableCoupons.stream()
        .map(UserCoupon::getCouponId)
        .distinct()
        .toList();

    Map<Long, Coupon> couponMap = couponRepository.findAllByIds(couponIds).stream()
        .collect(Collectors.toMap(Coupon::getId, coupon -> coupon));

    return usableCoupons.stream()
        .map(userCoupon -> {
          Coupon coupon = couponMap.get(userCoupon.getCouponId());
          if (coupon == null) {
            return null;
          }

          return new Output(
              userCoupon.getId(),
              userCoupon.getUserId(),
              userCoupon.getCouponId(),
              coupon.getName(),
              coupon.getDiscountAmount(),
              userCoupon.getIssuedAt(),
              userCoupon.getExpiredAt()
          );
        })
        .filter(Objects::nonNull)
        .toList();
  }

  @Schema(description = "사용자 쿠폰 정보")
  public record Output(
      @Schema(description = "사용자 쿠폰 ID", example = "1")
      Long userCouponId,

      @Schema(description = "사용자 ID", example = "1")
      Long userId,

      @Schema(description = "쿠폰 ID", example = "1")
      Long couponId,

      @Schema(description = "쿠폰명", example = "신규 회원 할인 쿠폰")
      String couponName,

      @Schema(description = "할인 금액", example = "5000")
      Long discountAmount,

      @Schema(description = "발급일시", example = "2025-01-20T15:30:00")
      LocalDateTime issuedAt,

      @Schema(description = "만료일시", example = "2025-01-27T15:30:00")
      LocalDateTime expiredAt) {}
}
