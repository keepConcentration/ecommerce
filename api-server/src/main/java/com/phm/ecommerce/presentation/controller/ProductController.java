package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.product.GetPopularProductsUseCase;
import com.phm.ecommerce.application.usecase.product.GetProductByIdUseCase;
import com.phm.ecommerce.application.usecase.product.GetProductsUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.ProductApi;
import com.phm.ecommerce.presentation.dto.response.PageResponse;
import com.phm.ecommerce.presentation.dto.response.PopularProductResponse;
import com.phm.ecommerce.presentation.dto.response.ProductResponse;
import com.phm.ecommerce.presentation.mapper.ProductMapper;
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
  public ApiResponse<List<PopularProductResponse>> getPopularProducts() {
    List<GetPopularProductsUseCase.Output> outputs = getPopularProductsUseCase.execute();
    return ApiResponse.success(productMapper.toPopularProductResponses(outputs));
  }
}
