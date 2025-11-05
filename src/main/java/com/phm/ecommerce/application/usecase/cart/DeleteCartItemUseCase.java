package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.cart.exception.CartItemOwnershipViolationException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCartItemUseCase {

  private final CartItemRepository cartItemRepository;

  public record Input(Long cartItemId, Long userId) {}

  public void execute(Input input) {
    CartItem cartItem = cartItemRepository.findByIdOrThrow(input.cartItemId());
    if (!cartItem.getUserId().equals(input.userId())) {
      throw new CartItemOwnershipViolationException();
    }

    cartItemRepository.deleteById(cartItem.getId());
  }
}
