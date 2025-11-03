package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.cart.exception.CartItemNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCartItemUseCase {

  private final CartItemRepository cartItemRepository;

  public record Input(Long cartItemId) {}

  public void execute(Input input) {
    CartItem cartItem = cartItemRepository.findById(input.cartItemId())
        .orElseThrow(CartItemNotFoundException::new);

    cartItemRepository.deleteById(cartItem.getId());
  }
}
