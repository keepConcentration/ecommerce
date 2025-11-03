package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.coupon.GetUserCouponsUseCase;
import com.phm.ecommerce.application.usecase.coupon.IssueCouponUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.CouponApi;
import com.phm.ecommerce.presentation.dto.request.IssueCouponRequest;
import com.phm.ecommerce.presentation.dto.response.UserCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

  private final IssueCouponUseCase issueCouponUseCase;
  private final GetUserCouponsUseCase getUserCouponsUseCase;

  @Override
  public ResponseEntity<ApiResponse<UserCouponResponse>> issueCoupon(
      Long couponId, IssueCouponRequest request) {
    UserCouponResponse userCoupon = issueCouponUseCase.execute(couponId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userCoupon));
  }

  @Override
  public ApiResponse<List<UserCouponResponse>> getUserCoupons(Long userId) {
    List<UserCouponResponse> coupons = getUserCouponsUseCase.execute(userId);
    return ApiResponse.success(coupons);
  }
}
