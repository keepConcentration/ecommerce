package com.phm.ecommerce.persistence.repository;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

  Product save(Product product);

  Optional<Product> findById(Long id);

  default Product findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(ProductNotFoundException::new);
  }

  List<Product> findAll();

  List<Product> findAllByIds(List<Long> ids);

  List<Product> findTopByViewCount(int limit);

  void deleteById(Long id);
}
