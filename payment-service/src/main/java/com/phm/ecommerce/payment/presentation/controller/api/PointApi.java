package com.phm.ecommerce.payment.presentation.controller.api;

import com.phm.ecommerce.payment.presentation.common.ApiResponse;
import com.phm.ecommerce.payment.presentation.dto.request.ChargePointsRequest;
import com.phm.ecommerce.payment.presentation.dto.response.ChargedPointResponse;
import com.phm.ecommerce.payment.presentation.dto.response.PointResponse;
import com.phm.ecommerce.payment.presentation.dto.response.PointTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Points", description = "포인트 관리 API")
@RequestMapping("/api/v1")
public interface PointApi {

  @GetMapping("/points")
  @Operation(summary = "포인트 잔액 조회", description = "사용자의 현재 포인트 잔액을 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<PointResponse> getPoints(
      @Parameter(description = "사용자 ID", required = true, example = "1") @RequestParam Long userId);

  @PostMapping("/points/charge")
  @Operation(summary = "포인트 충전", description = "포인트를 충전합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "충전 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 금액",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<ChargedPointResponse> chargePoints(@Valid @RequestBody ChargePointsRequest request);

  @GetMapping("/transactions")
  @Operation(summary = "포인트 거래 내역 조회", description = "포인트 충전/사용 내역을 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<List<PointTransactionResponse>> getPointTransactions(
      @Parameter(description = "사용자 ID", required = true, example = "1") @RequestParam Long userId);
}
