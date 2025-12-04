package com.phm.ecommerce.presentation.controller.api;

import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.dto.request.IssueCouponRequest;
import com.phm.ecommerce.presentation.dto.response.UserCouponResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupons", description = "쿠폰 발급 및 조회 API")
@RequestMapping("/api/v1/coupons")
public interface CouponApi {

  @PostMapping("/{couponId}/request")
  @Operation(summary = "쿠폰 발급 요청", description = "선착순 쿠폰 발급을 요청합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "쿠폰 발급 요청 접수",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 발급 요청된 쿠폰",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ResponseEntity<ApiResponse<Object>> requestCouponIssue(
      @Parameter(description = "쿠폰 ID", required = true, example = "1") @PathVariable Long couponId,
      @Valid @RequestBody IssueCouponRequest request);

  @GetMapping
  @Operation(summary = "보유 쿠폰 조회", description = "사용 가능한 쿠폰 목록을 조회합니다. (미사용 + 미만료)")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<List<UserCouponResponse>> getUserCoupons(
      @Parameter(description = "사용자 ID", required = true, example = "1") @RequestParam Long userId);
}
