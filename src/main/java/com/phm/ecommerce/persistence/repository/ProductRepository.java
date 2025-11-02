package com.phm.ecommerce.persistence.repository;

import com.phm.ecommerce.domain.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

  Product save(Product product);

  Optional<Product> findById(Long id);

  List<Product> findAll();

  List<Product> findTopByViewCount(int limit);

  void deleteById(Long id);
}
