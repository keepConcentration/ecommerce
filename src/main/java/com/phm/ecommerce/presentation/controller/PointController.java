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
    GetPointsUseCase.Output output = getPointsUseCase.execute(new GetPointsUseCase.Input(userId));
    PointResponse point = new PointResponse(output.pointId(), output.userId(), output.amount(), output.updatedAt());
    return ApiResponse.success(point);
  }

  @Override
  public ApiResponse<ChargedPointResponse> chargePoints(ChargePointsRequest request) {
    ChargePointsUseCase.Output output = chargePointsUseCase.execute(
        new ChargePointsUseCase.Input(request.userId(), request.amount()));
    ChargedPointResponse chargedPoint = new ChargedPointResponse(output.pointId(), output.userId(),
        output.amount(), output.chargedAmount(), output.transactionId(), output.createdAt());
    return ApiResponse.success(chargedPoint);
  }

  @Override
  public ApiResponse<List<PointTransactionResponse>> getPointTransactions(Long userId) {
    List<GetPointTransactionsUseCase.Output> outputs = getPointTransactionsUseCase.execute(
        new GetPointTransactionsUseCase.Input(userId));
    List<PointTransactionResponse> transactions = outputs.stream()
        .map(output -> new PointTransactionResponse(output.transactionId(), output.pointId(),
            output.orderId(), output.amount(), output.createdAt()))
        .toList();
    return ApiResponse.success(transactions);
  }
}
