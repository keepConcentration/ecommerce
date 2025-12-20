package com.phm.ecommerce.common.infrastructure.repository;

import com.phm.ecommerce.common.domain.cart.CartItem;
import com.phm.ecommerce.common.domain.cart.exception.CartItemNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  default CartItem findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(CartItemNotFoundException::new);
  }

  Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

  List<CartItem> findByUserId(Long userId);

  void deleteByUserId(Long userId);
}
