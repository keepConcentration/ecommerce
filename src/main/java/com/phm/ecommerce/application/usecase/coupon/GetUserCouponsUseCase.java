package com.phm.ecommerce.application.usecase.coupon;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
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

  public record Input(Long userId) {}

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

  public record Output(
      Long userCouponId,
      Long userId,
      Long couponId,
      String couponName,
      Long discountAmount,
      LocalDateTime issuedAt,
      LocalDateTime expiredAt) {}
}
