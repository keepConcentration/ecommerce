package com.phm.ecommerce.infrastructure.repository;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

  default Product findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(ProductNotFoundException::new);
  }

  @Query("SELECT p FROM Product p WHERE p.id IN :ids")
  List<Product> findAllByIds(@Param("ids") List<Long> ids);

  @Query(value = "SELECT * FROM products ORDER BY (view_count * 0.1 + sales_count * 0.9) DESC LIMIT :limit", nativeQuery = true)
  List<Product> findTopByPopularityScore(@Param("limit") int limit);

  default List<Product> findPopularProducts(int limit) {
    return findTopByPopularityScore(limit);
  }
}
