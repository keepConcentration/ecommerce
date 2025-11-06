package com.phm.ecommerce.infrastructure.repository;


import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.cart.exception.CartItemNotFoundException;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository {

  CartItem save(CartItem cartItem);

  Optional<CartItem> findById(Long id);

  default CartItem findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(CartItemNotFoundException::new);
  }

  Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

  List<CartItem> findByUserId(Long userId);

  void deleteById(Long id);

  void deleteByUserId(Long userId);
}
