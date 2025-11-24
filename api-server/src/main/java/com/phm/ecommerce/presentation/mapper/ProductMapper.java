package com.phm.ecommerce.presentation.mapper;

import com.phm.ecommerce.application.usecase.product.GetPopularProductsUseCase;
import com.phm.ecommerce.application.usecase.product.GetProductByIdUseCase;
import com.phm.ecommerce.application.usecase.product.GetProductsUseCase;
import com.phm.ecommerce.presentation.dto.response.PopularProductResponse;
import com.phm.ecommerce.presentation.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

  public List<ProductResponse> toResponses(List<GetProductsUseCase.Output> outputs) {
    return outputs.stream()
        .map(output -> new ProductResponse(
            output.productId(),
            output.name(),
            output.price(),
            output.quantity(),
            output.viewCount(),
            output.createdAt(),
            output.updatedAt()))
        .toList();
  }

  public GetProductByIdUseCase.Input toInput(Long productId) {
    return new GetProductByIdUseCase.Input(productId);
  }

  public ProductResponse toResponse(GetProductByIdUseCase.Output output) {
    return new ProductResponse(
        output.productId(),
        output.name(),
        output.price(),
        output.quantity(),
        output.viewCount(),
        output.createdAt(),
        output.updatedAt());
  }

  public List<PopularProductResponse> toPopularProductResponses(
      List<GetPopularProductsUseCase.Output> outputs) {
    return outputs.stream()
        .map(output -> new PopularProductResponse(
            output.productId(),
            output.name(),
            output.price(),
            output.quantity(),
            output.viewCount(),
            output.salesCount(),
            output.popularityScore(),
            output.createdAt(),
            output.updatedAt()))
        .toList();
  }
}
