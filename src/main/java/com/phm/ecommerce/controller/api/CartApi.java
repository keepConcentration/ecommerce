package com.phm.ecommerce.controller.api;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.dto.request.AddCartItemRequest;
import com.phm.ecommerce.dto.request.UpdateQuantityRequest;
import com.phm.ecommerce.dto.response.CartResponse;
import com.phm.ecommerce.dto.response.CartItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "장바구니 관리 API")
public interface CartApi {

  @PutMapping("/items")
  @Operation(summary = "장바구니 상품 추가", description = "장바구니에 상품을 추가하거나 수량을 증가시킵니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "생성됨",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(
      @Valid @RequestBody AddCartItemRequest request);

  @GetMapping("/items")
  @Operation(summary = "장바구니 조회", description = "사용자의 장바구니 목록을 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<CartResponse> getCartItems(
      @Parameter(description = "사용자 ID", required = true, example = "1") @RequestParam Long userId);

  @PatchMapping("/items/{cartItemId}")
  @Operation(summary = "장바구니 수량 변경", description = "장바구니 상품의 수량을 변경합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "장바구니 아이템을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 수량",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<CartItemResponse> updateCartItemQuantity(
      @Parameter(description = "장바구니 아이템 ID", required = true, example = "1") @PathVariable
          Long cartItemId,
      @Valid @RequestBody UpdateQuantityRequest request);

  @DeleteMapping("/items/{cartItemId}")
  @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 특정 상품을 삭제합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "삭제 성공 (응답 본문 없음)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "장바구니 아이템을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ResponseEntity<Void> deleteCartItem(
      @Parameter(description = "장바구니 아이템 ID", required = true, example = "1") @PathVariable
          Long cartItemId);
}
