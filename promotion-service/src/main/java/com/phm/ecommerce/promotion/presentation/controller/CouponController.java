package com.phm.ecommerce.promotion.presentation.controller;

import com.phm.ecommerce.promotion.application.usecase.coupon.GetUserCouponsUseCase;
import com.phm.ecommerce.promotion.application.usecase.coupon.RequestCouponIssueUseCase;
import com.phm.ecommerce.promotion.presentation.common.ApiResponse;
import com.phm.ecommerce.promotion.presentation.controller.api.CouponApi;
import com.phm.ecommerce.promotion.presentation.dto.request.IssueCouponRequest;
import com.phm.ecommerce.promotion.presentation.dto.response.UserCouponResponse;
import com.phm.ecommerce.promotion.presentation.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

  private final RequestCouponIssueUseCase requestCouponIssueUseCase;
  private final GetUserCouponsUseCase getUserCouponsUseCase;
  private final CouponMapper couponMapper;

  @Override
  public ResponseEntity<ApiResponse<Object>> requestCouponIssue(
      Long couponId, IssueCouponRequest request) {
    RequestCouponIssueUseCase.Output output = requestCouponIssueUseCase.execute(
        couponMapper.toRequestInput(couponId, request));
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.success(output));
  }

  @Override
  public ApiResponse<List<UserCouponResponse>> getUserCoupons(Long userId) {
    List<GetUserCouponsUseCase.Output> outputs = getUserCouponsUseCase.execute(
        couponMapper.toInput(userId));
    return ApiResponse.success(couponMapper.toResponses(outputs));
  }
}
