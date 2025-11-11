package com.phm.ecommerce.infrastructure.repository.inmemory;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.infrastructure.repository.CartItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCartItemRepository implements CartItemRepository {

  private final Map<Long, CartItem> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public CartItem save(CartItem cartItem) {
    if (cartItem.getId() == null) {
      CartItem newCartItem = CartItem.reconstruct(
              idGenerator.getAndIncrement(),
              cartItem.getUserId(),
              cartItem.getProductId(),
              cartItem.getQuantity());
      store.put(newCartItem.getId(), newCartItem);
      return newCartItem;
    }
    store.put(cartItem.getId(), cartItem);
    return cartItem;
  }

  @Override
  public Optional<CartItem> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId) {
    return store.values().stream()
        .filter(item -> item.getUserId().equals(userId) && item.getProductId().equals(productId))
        .findFirst();
  }

  @Override
  public List<CartItem> findByUserId(Long userId) {
    return store.values().stream().filter(item -> item.getUserId().equals(userId)).toList();
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }

  @Override
  public void deleteByUserId(Long userId) {
    store.values().removeIf(item -> item.getUserId().equals(userId));
  }
}
