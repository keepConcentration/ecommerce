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

  @Query("SELECT p FROM Product p ORDER BY (p.viewCount * :viewWeight + p.salesCount * :salesWeight) DESC")
  List<Product> findTopByPopularity(@Param("viewWeight") Double viewWeight,
                                     @Param("salesWeight") Double salesWeight);

  default List<Product> findPopularProducts(int limit, Double viewWeight, Double salesWeight) {
    List<Product> products = findTopByPopularity(viewWeight, salesWeight);
    return products.stream().limit(limit).toList();
  }
}
