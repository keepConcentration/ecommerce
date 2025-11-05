package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCartItemUseCase {

  private final CartItemRepository cartItemRepository;

  public record Input(Long cartItemId, Long userId) {}

  public void execute(Input input) {
    List<CartItem> cartItems = cartItemRepository.findByUserId(input.userId());

    cartItems.stream()
        .filter(item -> item.getId().equals(input.cartItemId()))
        .forEach(item -> cartItemRepository.deleteById(item.getId()));
  }
}
