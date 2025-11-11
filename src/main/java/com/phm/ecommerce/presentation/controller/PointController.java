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
import com.phm.ecommerce.presentation.mapper.PointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PointController implements PointApi {

  private final GetPointsUseCase getPointsUseCase;
  private final ChargePointsUseCase chargePointsUseCase;
  private final GetPointTransactionsUseCase getPointTransactionsUseCase;
  private final PointMapper pointMapper;

  @Override
  public ApiResponse<PointResponse> getPoints(Long userId) {
    GetPointsUseCase.Output output = getPointsUseCase.execute(pointMapper.toInput(userId));
    return ApiResponse.success(pointMapper.toResponse(output));
  }

  @Override
  public ApiResponse<ChargedPointResponse> chargePoints(ChargePointsRequest request) {
    ChargePointsUseCase.Output output = chargePointsUseCase.execute(pointMapper.toInput(request));
    return ApiResponse.success(pointMapper.toResponse(output));
  }

  @Override
  public ApiResponse<List<PointTransactionResponse>> getPointTransactions(Long userId) {
    List<GetPointTransactionsUseCase.Output> outputs = getPointTransactionsUseCase.execute(
        pointMapper.toTransactionInput(userId));
    return ApiResponse.success(pointMapper.toTransactionResponses(outputs));
  }
}
