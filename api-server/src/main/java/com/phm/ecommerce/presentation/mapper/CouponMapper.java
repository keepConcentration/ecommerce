package com.phm.ecommerce.presentation.mapper;

import com.phm.ecommerce.application.usecase.coupon.GetUserCouponsUseCase;
import com.phm.ecommerce.application.usecase.coupon.RequestCouponIssueUseCase;
import com.phm.ecommerce.presentation.dto.request.IssueCouponRequest;
import com.phm.ecommerce.presentation.dto.response.UserCouponResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CouponMapper {

  public RequestCouponIssueUseCase.Input toRequestInput(Long couponId, IssueCouponRequest request) {
    return new RequestCouponIssueUseCase.Input(
        couponId,
        request.userId());
  }

  public GetUserCouponsUseCase.Input toInput(Long userId) {
    return new GetUserCouponsUseCase.Input(userId);
  }

  public List<UserCouponResponse> toResponses(List<GetUserCouponsUseCase.Output> outputs) {
    return outputs.stream()
        .map(output -> new UserCouponResponse(
            output.userCouponId(),
            output.userId(),
            output.couponId(),
            output.couponName(),
            output.discountAmount(),
            output.issuedAt(),
            output.expiredAt()))
        .toList();
  }
}
