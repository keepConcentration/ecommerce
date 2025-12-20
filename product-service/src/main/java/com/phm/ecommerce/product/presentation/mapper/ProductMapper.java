package com.phm.ecommerce.product.presentation.mapper;

import com.phm.ecommerce.product.application.usecase.product.GetPopularProductsUseCase;
import com.phm.ecommerce.product.application.usecase.product.GetProductByIdUseCase;
import com.phm.ecommerce.product.application.usecase.product.GetProductsUseCase;
import com.phm.ecommerce.product.presentation.dto.request.PopularProductRequest;
import com.phm.ecommerce.product.presentation.dto.response.PageResponse;
import com.phm.ecommerce.product.presentation.dto.response.PopularProductResponse;
import com.phm.ecommerce.product.presentation.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

  public PageResponse<ProductResponse> toPageResponse(Page<GetProductsUseCase.Output> pageOutput) {
    List<ProductResponse> content = pageOutput.getContent().stream()
        .map(output -> new ProductResponse(
            output.productId(),
            output.name(),
            output.price(),
            output.quantity(),
            output.viewCount(),
            output.createdAt(),
            output.updatedAt()))
        .toList();

    return new PageResponse<>(
        content,
        pageOutput.getNumber(),
        pageOutput.getSize(),
        pageOutput.getTotalElements(),
        pageOutput.getTotalPages(),
        pageOutput.isFirst(),
        pageOutput.isLast());
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
            output.createdAt(),
            output.updatedAt()))
        .toList();
  }

  public GetPopularProductsUseCase.Input toInput(PopularProductRequest request) {
    return new GetPopularProductsUseCase.Input(request.limit());
  }
}
