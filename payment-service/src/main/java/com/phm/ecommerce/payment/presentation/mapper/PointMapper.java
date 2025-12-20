package com.phm.ecommerce.payment.presentation.mapper;

import com.phm.ecommerce.payment.application.usecase.point.ChargePointsUseCase;
import com.phm.ecommerce.payment.application.usecase.point.GetPointTransactionsUseCase;
import com.phm.ecommerce.payment.application.usecase.point.GetPointsUseCase;
import com.phm.ecommerce.payment.presentation.dto.request.ChargePointsRequest;
import com.phm.ecommerce.payment.presentation.dto.response.ChargedPointResponse;
import com.phm.ecommerce.payment.presentation.dto.response.PointResponse;
import com.phm.ecommerce.payment.presentation.dto.response.PointTransactionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PointMapper {

  public GetPointsUseCase.Input toInput(Long userId) {
    return new GetPointsUseCase.Input(userId);
  }

  public PointResponse toResponse(GetPointsUseCase.Output output) {
    return new PointResponse(
        output.pointId(),
        output.userId(),
        output.amount(),
        output.updatedAt());
  }

  public ChargePointsUseCase.Input toInput(ChargePointsRequest request) {
    return new ChargePointsUseCase.Input(
        request.userId(),
        request.amount());
  }

  public ChargedPointResponse toResponse(ChargePointsUseCase.Output output) {
    return new ChargedPointResponse(
        output.pointId(),
        output.userId(),
        output.amount(),
        output.chargedAmount(),
        output.transactionId(),
        output.createdAt());
  }

  public GetPointTransactionsUseCase.Input toTransactionInput(Long userId) {
    return new GetPointTransactionsUseCase.Input(userId);
  }

  public List<PointTransactionResponse> toTransactionResponses(List<GetPointTransactionsUseCase.Output> outputs) {
    return outputs.stream()
        .map(output -> new PointTransactionResponse(
            output.transactionId(),
            output.pointId(),
            output.orderId(),
            output.amount(),
            output.createdAt()))
        .toList();
  }
}
