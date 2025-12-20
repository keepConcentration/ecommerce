package com.phm.ecommerce.product.presentation.controller.api;

import com.phm.ecommerce.product.presentation.common.ApiResponse;
import com.phm.ecommerce.product.presentation.dto.request.PopularProductRequest;
import com.phm.ecommerce.product.presentation.dto.response.PageResponse;
import com.phm.ecommerce.product.presentation.dto.response.PopularProductResponse;
import com.phm.ecommerce.product.presentation.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Products", description = "상품 관리 API")
@RequestMapping("/api/v1/products")
public interface ProductApi {

  @GetMapping
  @Operation(summary = "상품 목록 조회", description = "페이지네이션을 지원하는 상품 목록을 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<PageResponse<ProductResponse>> getProducts(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "페이지당 항목 수 (최대 100)", example = "20")
          @RequestParam(defaultValue = "20")
          int size);

  @GetMapping("/{productId}")
  @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다. 조회 시 해당 상품의 조회수가 1 증가합니다.")
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
  @Operation(
      summary = "인기 상품 조회",
      description = "조회수와 판매량에 가중치를 적용한 인기 상품 목록을 조회합니다.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  ApiResponse<List<PopularProductResponse>> getPopularProducts(PopularProductRequest request);
}
