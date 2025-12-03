package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPopularProductsUseCase {

  private final ProductService productService;

  public record Input(
      LocalDate date,
      Integer limit
  ) {
    public Input {
      if (date == null) {
        date = LocalDate.now();
      }
    }
  }

  public List<Output> execute(Input input) {
    ProductService.ProductIdList productIdList = productService.getPopularProductIds(input.date(), input.limit());

    return productIdList.ids().stream()
        .map(productId -> {
          ProductService.ProductInfo product = productService.getProduct(productId);
          return new Output(
              product.id(),
              product.name(),
              product.price(),
              product.quantity(),
              product.viewCount(),
              product.salesCount(),
              product.createdAt(),
              product.updatedAt()
          );
        })
        .toList();
  }

  public record Output(
      Long productId,
      String name,
      Long price,
      Long quantity,
      Long viewCount,
      Long salesCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
}
