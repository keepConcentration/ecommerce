package com.phm.ecommerce.controller;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.controller.api.ProductApi;
import com.phm.ecommerce.dto.response.PopularProductResponse;
import com.phm.ecommerce.dto.response.ProductResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductApi {

  @Override
  public ApiResponse<List<ProductResponse>> getProducts() {
    List<ProductResponse> products =
        List.of(
            ProductResponse.builder()
                .productId(1L)
                .name("노트북")
                .price(1500000L)
                .quantity(50L)
                .viewCount(1523L)
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 20, 15, 30))
                .build(),
            ProductResponse.builder()
                .productId(2L)
                .name("마우스")
                .price(35000L)
                .quantity(0L)
                .viewCount(842L)
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 20, 15, 30))
                .build());
    return ApiResponse.success(products);
  }

  @Override
  public ApiResponse<ProductResponse> getProductById(Long productId) {
    ProductResponse product =
        ProductResponse.builder()
            .productId(productId)
            .name("노트북")
            .price(1500000L)
            .quantity(50L)
            .viewCount(1523L)
            .createdAt(LocalDateTime.of(2025, 1, 15, 10, 0))
            .updatedAt(LocalDateTime.of(2025, 1, 20, 15, 30))
            .build();
    return ApiResponse.success(product);
  }

  @Override
  public ApiResponse<List<PopularProductResponse>> getPopularProducts() {
    List<PopularProductResponse> popularProducts =
        List.of(
            PopularProductResponse.builder()
                .productId(1L)
                .name("노트북")
                .price(1500000L)
                .totalSales(150L)
                .build(),
            PopularProductResponse.builder()
                .productId(3L)
                .name("키보드")
                .price(120000L)
                .totalSales(98L)
                .build());
    return ApiResponse.success(popularProducts);
  }

  @Override
  public ApiResponse<List<ProductResponse>> getMostViewedProducts() {
    List<ProductResponse> mostViewedProducts =
        List.of(
            ProductResponse.builder()
                .productId(1L)
                .name("노트북")
                .price(1500000L)
                .quantity(50L)
                .viewCount(15230L)
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 20, 15, 30))
                .build(),
            ProductResponse.builder()
                .productId(5L)
                .name("모니터")
                .price(450000L)
                .quantity(30L)
                .viewCount(12850L)
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 20, 15, 30))
                .build(),
            ProductResponse.builder()
                .productId(3L)
                .name("키보드")
                .price(120000L)
                .quantity(100L)
                .viewCount(9870L)
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 20, 15, 30))
                .build());
    return ApiResponse.success(mostViewedProducts);
  }
}
