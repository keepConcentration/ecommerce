package com.phm.ecommerce.controller;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.controller.api.PointApi;
import com.phm.ecommerce.dto.request.ChargePointsRequest;
import com.phm.ecommerce.dto.response.ChargedPointResponse;
import com.phm.ecommerce.dto.response.PointResponse;
import com.phm.ecommerce.dto.response.PointTransactionResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PointController implements PointApi {

  @Override
  public ApiResponse<PointResponse> getPoints(Long userId) {
    PointResponse point =
        PointResponse.builder()
            .pointId(1L)
            .userId(userId)
            .amount(50000L)
            .updatedAt(LocalDateTime.of(2025, 1, 20, 10, 0))
            .build();
    return ApiResponse.success(point);
  }

  @Override
  public ApiResponse<ChargedPointResponse> chargePoints(ChargePointsRequest request) {
    ChargedPointResponse chargedPoint =
        ChargedPointResponse.builder()
            .pointId(1L)
            .userId(request.getUserId())
            .amount(150000L)
            .chargedAmount(request.getAmount())
            .pointTransactionId(123L)
            .createdAt(LocalDateTime.of(2025, 1, 20, 15, 0))
            .build();
    return ApiResponse.success(chargedPoint);
  }

  @Override
  public ApiResponse<List<PointTransactionResponse>> getPointTransactions(Long userId) {
    List<PointTransactionResponse> transactions =
        List.of(
            PointTransactionResponse.builder()
                .pointTransactionId(125L)
                .pointId(1L)
                .amount(-2980000L)
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30))
                .build(),
            PointTransactionResponse.builder()
                .pointTransactionId(124L)
                .pointId(1L)
                .amount(100000L)
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 0))
                .build());
    return ApiResponse.success(transactions);
  }
}
