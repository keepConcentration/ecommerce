package com.phm.ecommerce.application.usecase.product;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase {

  private final ProductRepository productRepository;

  public ProductResponse execute(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(ProductNotFoundException::new);

    product.increaseViewCount();
    product = productRepository.save(product);

    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getQuantity(),
        product.getViewCount(),
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }
}
