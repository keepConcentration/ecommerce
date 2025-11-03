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
    IssueCouponUseCase.Output output = issueCouponUseCase.execute(
        new IssueCouponUseCase.Input(couponId, request.userId()));
    UserCouponResponse userCoupon = new UserCouponResponse(output.userCouponId(), output.userId(),
        output.couponId(), output.couponName(), output.discountAmount(), output.issuedAt(), output.expiredAt());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userCoupon));
  }

  @Override
  public ApiResponse<List<UserCouponResponse>> getUserCoupons(Long userId) {
    List<GetUserCouponsUseCase.Output> outputs = getUserCouponsUseCase.execute(
        new GetUserCouponsUseCase.Input(userId));
    List<UserCouponResponse> coupons = outputs.stream()
        .map(output -> new UserCouponResponse(output.userCouponId(), output.userId(), output.couponId(),
            output.couponName(), output.discountAmount(), output.issuedAt(), output.expiredAt()))
        .toList();
    return ApiResponse.success(coupons);
  }
}
