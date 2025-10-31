package com.phm.ecommerce.controller.api;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.dto.response.PopularProductResponse;
import com.phm.ecommerce.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Products", description = "상품 관리 API")
public interface ProductApi {

  @GetMapping
  @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<List<ProductResponse>> getProducts();

  @GetMapping("/{productId}")
  @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<ProductResponse> getProductById(
      @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable
          Long productId);

  @GetMapping("/popular")
  @Operation(summary = "인기 상품 조회", description = "최근 3일간 판매량 기준 상위 5개 상품을 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<List<PopularProductResponse>> getPopularProducts();
}
