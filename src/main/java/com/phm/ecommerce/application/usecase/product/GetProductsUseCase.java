package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProductsUseCase {

  private final ProductRepository productRepository;

  public List<ProductResponse> execute() {
    List<Product> products = productRepository.findAll();

    return products.stream()
        .map(product -> new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getViewCount(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        ))
        .toList();
  }
}
