package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.cart.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository {

  CartItem save(CartItem cartItem);

  Optional<CartItem> findById(Long id);

  Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

  List<CartItem> findByUserId(Long userId);

  void deleteById(Long id);

  void deleteByUserId(Long userId);
}
