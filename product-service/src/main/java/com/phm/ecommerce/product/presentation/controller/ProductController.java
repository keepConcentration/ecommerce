package com.phm.ecommerce.product.presentation.controller;

import com.phm.ecommerce.product.application.usecase.product.GetPopularProductsUseCase;
import com.phm.ecommerce.product.application.usecase.product.GetProductByIdUseCase;
import com.phm.ecommerce.product.application.usecase.product.GetProductsUseCase;
import com.phm.ecommerce.product.presentation.common.ApiResponse;
import com.phm.ecommerce.product.presentation.controller.api.ProductApi;
import com.phm.ecommerce.product.presentation.dto.request.PopularProductRequest;
import com.phm.ecommerce.product.presentation.dto.response.PageResponse;
import com.phm.ecommerce.product.presentation.dto.response.PopularProductResponse;
import com.phm.ecommerce.product.presentation.dto.response.ProductResponse;
import com.phm.ecommerce.product.presentation.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

  private final GetProductsUseCase getProductsUseCase;
  private final GetProductByIdUseCase getProductByIdUseCase;
  private final GetPopularProductsUseCase getPopularProductsUseCase;
  private final ProductMapper productMapper;

  @Override
  public ApiResponse<PageResponse<ProductResponse>> getProducts(int page, int size) {
    GetProductsUseCase.Input input = new GetProductsUseCase.Input(page, size);
    Page<GetProductsUseCase.Output> pageOutput = getProductsUseCase.execute(input);
    return ApiResponse.success(productMapper.toPageResponse(pageOutput));
  }

  @Override
  public ApiResponse<ProductResponse> getProductById(Long productId) {
    GetProductByIdUseCase.Output output = getProductByIdUseCase.execute(
        productMapper.toInput(productId));
    return ApiResponse.success(productMapper.toResponse(output));
  }

  @Override
  public ApiResponse<List<PopularProductResponse>> getPopularProducts(PopularProductRequest request) {
    List<GetPopularProductsUseCase.Output> outputs = getPopularProductsUseCase.execute(
        productMapper.toInput(request));
    return ApiResponse.success(productMapper.toPopularProductResponses(outputs));
  }
}
