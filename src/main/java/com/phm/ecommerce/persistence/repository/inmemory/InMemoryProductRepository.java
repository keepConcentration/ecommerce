package com.phm.ecommerce.persistence.repository.inmemory;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductRepository implements ProductRepository {

  private final Map<Long, Product> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public Product save(Product product) {
    if (product.getId() == null) {
      Product newProduct =
          new Product(
              idGenerator.getAndIncrement(),
              product.getName(),
              product.getPrice(),
              product.getQuantity(),
              product.getViewCount());
      store.put(newProduct.getId(), newProduct);
      return newProduct;
    }
    store.put(product.getId(), product);
    return product;
  }

  @Override
  public Optional<Product> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<Product> findAll() {
    return List.copyOf(store.values());
  }

  @Override
  public List<Product> findAllByIds(List<Long> ids) {
    return ids.stream()
        .map(store::get)
        .filter(Objects::nonNull)
        .toList();
  }

  @Override
  public List<Product> findTopByViewCount(int limit) {
    return store.values().stream()
        .sorted(Comparator.comparing(Product::getViewCount).reversed())
        .limit(limit)
        .toList();
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
