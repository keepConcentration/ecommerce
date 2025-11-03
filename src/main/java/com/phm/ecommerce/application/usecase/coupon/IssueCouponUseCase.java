package com.phm.ecommerce.application.usecase.coupon;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyIssuedException;
import com.phm.ecommerce.domain.coupon.exception.CouponNotFoundException;
import com.phm.ecommerce.domain.coupon.exception.CouponSoldOutException;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IssueCouponUseCase {

  private final CouponRepository couponRepository;
  private final UserCouponRepository userCouponRepository;

  @Schema(description = "쿠폰 발급 요청")
  public record Input(
      @Schema(description = "쿠폰 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "쿠폰 ID는 필수입니다")
      Long couponId,

      @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId) {}

  // TODO 동시성 이슈 처리
  public Output execute(Input request) {
    Coupon coupon = couponRepository.findById(request.couponId())
        .orElseThrow(CouponNotFoundException::new);

    boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(
        request.userId(), request.couponId());
    if (alreadyIssued) {
      throw new CouponAlreadyIssuedException();
    }

    if (!coupon.canIssue()) {
      throw new CouponSoldOutException();
    }
    coupon.issue();

    UserCoupon userCoupon = UserCoupon.issue(request.userId(), request.couponId(), coupon.getValidDays());
    userCoupon = userCouponRepository.save(userCoupon);

    couponRepository.save(coupon);

    return new Output(
        userCoupon.getId(),
        userCoupon.getUserId(),
        userCoupon.getCouponId(),
        coupon.getName(),
        coupon.getDiscountAmount(),
        userCoupon.getIssuedAt(),
        userCoupon.getExpiredAt()
    );
  }

  @Schema(description = "발급된 쿠폰 정보")
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
