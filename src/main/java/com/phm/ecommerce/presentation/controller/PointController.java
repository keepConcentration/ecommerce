package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.point.GetPointsUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.PointApi;
import com.phm.ecommerce.presentation.dto.request.ChargePointsRequest;
import com.phm.ecommerce.presentation.dto.response.ChargedPointResponse;
import com.phm.ecommerce.presentation.dto.response.PointResponse;
import com.phm.ecommerce.presentation.dto.response.PointTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PointController implements PointApi {

  private final GetPointsUseCase getPointsUseCase;

  @Override
  public ApiResponse<PointResponse> getPoints(Long userId) {
    PointResponse point = getPointsUseCase.execute(userId);
    return ApiResponse.success(point);
  }

  @Override
  public ApiResponse<ChargedPointResponse> chargePoints(ChargePointsRequest request) {
    ChargedPointResponse chargedPoint = new ChargedPointResponse(1L, request.userId(), 150000L, request.amount(), 123L, LocalDateTime.of(2025, 1, 20, 15, 0));
    return ApiResponse.success(chargedPoint);
  }

  @Override
  public ApiResponse<List<PointTransactionResponse>> getPointTransactions(Long userId) {
    List<PointTransactionResponse> transactions =
        List.of(
            new PointTransactionResponse(125L, 1L, 1L, -2980000L, LocalDateTime.of(2025, 1, 20, 15, 30)),
            new PointTransactionResponse(124L, 1L, null, 100000L, LocalDateTime.of(2025, 1, 20, 15, 0)));
    return ApiResponse.success(transactions);
  }
}
