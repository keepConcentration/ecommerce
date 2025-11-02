package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.CouponApi;
import com.phm.ecommerce.presentation.dto.request.IssueCouponRequest;
import com.phm.ecommerce.presentation.dto.response.UserCouponResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class CouponController implements CouponApi {

  @Override
  public ResponseEntity<ApiResponse<UserCouponResponse>> issueCoupon(
      Long couponId, IssueCouponRequest request) {
    UserCouponResponse userCoupon = new UserCouponResponse(
        10L,
        request.getUserId(),
        couponId,
        "신규 가입 50000원 할인",
        50000L,
        LocalDateTime.of(2025, 1, 20, 10, 0),
        LocalDateTime.of(2025, 1, 27, 23, 59, 59));
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userCoupon));
  }

  @Override
  public ApiResponse<List<UserCouponResponse>> getUserCoupons(Long userId) {
    List<UserCouponResponse> coupons =
        List.of(
            new UserCouponResponse(
                11L,
                userId,
                2L,
                "10000원 할인",
                10000L,
                LocalDateTime.of(2025, 1, 18, 10, 0),
                LocalDateTime.of(2025, 1, 21, 23, 59, 59)),
            new UserCouponResponse(
                10L,
                userId,
                1L,
                "50000원 할인",
                10000L,
                LocalDateTime.of(2025, 1, 15, 10, 0),
                LocalDateTime.of(2025, 1, 22, 23, 59, 59)));
    return ApiResponse.success(coupons);
  }
}
