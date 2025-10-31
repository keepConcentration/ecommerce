package com.phm.ecommerce.controller;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.controller.api.CouponApi;
import com.phm.ecommerce.dto.request.IssueCouponRequest;
import com.phm.ecommerce.dto.response.UserCouponResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController implements CouponApi {

  @Override
  public ResponseEntity<ApiResponse<UserCouponResponse>> issueCoupon(
      Long couponId, IssueCouponRequest request) {
    UserCouponResponse userCoupon =
        UserCouponResponse.builder()
            .userCouponId(10L)
            .userId(request.getUserId())
            .couponId(couponId)
            .couponName("신규 가입 50000원 할인")
            .discountAmount(50000L)
            .issuedAt(LocalDateTime.of(2025, 1, 20, 10, 0))
            .expiredAt(LocalDateTime.of(2025, 1, 27, 23, 59, 59))
            .build();
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userCoupon));
  }

  @Override
  public ApiResponse<List<UserCouponResponse>> getUserCoupons(Long userId) {
    List<UserCouponResponse> coupons =
        List.of(
            UserCouponResponse.builder()
                .userCouponId(11L)
                .userId(userId)
                .couponId(2L)
                .couponName("10000원 할인")
                .discountAmount(10000L)
                .issuedAt(LocalDateTime.of(2025, 1, 18, 10, 0))
                .expiredAt(LocalDateTime.of(2025, 1, 21, 23, 59, 59))
                .build(),
            UserCouponResponse.builder()
                .userCouponId(10L)
                .userId(userId)
                .couponId(1L)
                .couponName("50000원 할인")
                .discountAmount(50000L)
                .issuedAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                .expiredAt(LocalDateTime.of(2025, 1, 22, 23, 59, 59))
                .build());
    return ApiResponse.success(coupons);
  }
}
