package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.ProductApi;
import com.phm.ecommerce.presentation.dto.response.PopularProductResponse;
import com.phm.ecommerce.presentation.dto.response.ProductResponse;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ProductController implements ProductApi {

  @Override
  public ApiResponse<List<ProductResponse>> getProducts() {
    List<ProductResponse> products =
        List.of(
            new ProductResponse(1L, "노트북", 1500000L, 50L, 1523L, LocalDateTime.of(2025, 1, 15, 10, 0), LocalDateTime.of(2025, 1, 20, 15, 30)),
            new ProductResponse(2L, "마우스", 35000L, 0L, 842L, LocalDateTime.of(2025, 1, 15, 10, 0), LocalDateTime.of(2025, 1, 20, 15, 30)));
    return ApiResponse.success(products);
  }

  @Override
  public ApiResponse<ProductResponse> getProductById(Long productId) {
    ProductResponse product = new ProductResponse(productId, "노트북", 1500000L, 50L, 1523L, LocalDateTime.of(2025, 1, 15, 10, 0), LocalDateTime.of(2025, 1, 20, 15, 30));
    return ApiResponse.success(product);
  }

  @Override
  public ApiResponse<List<PopularProductResponse>> getPopularProducts() {
    List<PopularProductResponse> popularProducts =
        List.of(
            new PopularProductResponse(1L, "노트북", 1500000L, 150L),
            new PopularProductResponse(3L, "키보드", 120000L, 98L));
    return ApiResponse.success(popularProducts);
  }

  @Override
  public ApiResponse<List<ProductResponse>> getMostViewedProducts() {
    List<ProductResponse> mostViewedProducts =
        List.of(
            new ProductResponse(1L, "노트북", 1500000L, 50L, 15230L, LocalDateTime.of(2025, 1, 15, 10, 0), LocalDateTime.of(2025, 1, 20, 15, 30)),
            new ProductResponse(5L, "모니터", 450000L, 30L, 12850L, LocalDateTime.of(2025, 1, 15, 10, 0), LocalDateTime.of(2025, 1, 20, 15, 30)),
            new ProductResponse(3L, "키보드", 120000L, 100L, 9870L, LocalDateTime.of(2025, 1, 15, 10, 0), LocalDateTime.of(2025, 1, 20, 15, 30)));
    return ApiResponse.success(mostViewedProducts);
  }
}
