package com.phm.ecommerce.controller.api;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.dto.request.CreateOrderRequest;
import com.phm.ecommerce.dto.request.DirectOrderRequest;
import com.phm.ecommerce.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Orders", description = "주문 및 결제 API")
public interface OrderApi {

  @PostMapping
  @Operation(
      summary = "주문 생성 및 결제",
      description = "장바구니의 상품들로 주문을 생성하고 포인트로 결제를 처리합니다. 쿠폰을 선택적으로 적용할 수 있습니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "주문 생성 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (쿠폰 만료, 사용됨)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "재고 부족 또는 포인트 부족",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ResponseEntity<ApiResponse<OrderResponse>> createOrder(
      @Valid @RequestBody CreateOrderRequest request);

  @PostMapping("/direct")
  @Operation(
      summary = "직접 주문 생성 및 결제",
      description = "장바구니 없이 상품을 직접 주문하고 포인트로 결제를 처리합니다. 쿠폰을 선택적으로 적용할 수 있습니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "주문 생성 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (쿠폰 만료, 사용됨)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "재고 부족 또는 포인트 부족",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ResponseEntity<ApiResponse<OrderResponse>> createDirectOrder(
      @Valid @RequestBody DirectOrderRequest request);
}
