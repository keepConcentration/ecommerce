package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.point.ChargePointsUseCase;
import com.phm.ecommerce.application.usecase.point.GetPointsUseCase;
import com.phm.ecommerce.application.usecase.point.GetPointTransactionsUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.PointApi;
import com.phm.ecommerce.presentation.dto.request.ChargePointsRequest;
import com.phm.ecommerce.presentation.dto.response.ChargedPointResponse;
import com.phm.ecommerce.presentation.dto.response.PointResponse;
import com.phm.ecommerce.presentation.dto.response.PointTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PointController implements PointApi {

  private final GetPointsUseCase getPointsUseCase;
  private final ChargePointsUseCase chargePointsUseCase;
  private final GetPointTransactionsUseCase getPointTransactionsUseCase;

  @Override
  public ApiResponse<PointResponse> getPoints(Long userId) {
    PointResponse point = getPointsUseCase.execute(userId);
    return ApiResponse.success(point);
  }

  @Override
  public ApiResponse<ChargedPointResponse> chargePoints(ChargePointsRequest request) {
    ChargedPointResponse chargedPoint = chargePointsUseCase.execute(request);
    return ApiResponse.success(chargedPoint);
  }

  @Override
  public ApiResponse<List<PointTransactionResponse>> getPointTransactions(Long userId) {
    List<PointTransactionResponse> transactions = getPointTransactionsUseCase.execute(userId);
    return ApiResponse.success(transactions);
  }
}
