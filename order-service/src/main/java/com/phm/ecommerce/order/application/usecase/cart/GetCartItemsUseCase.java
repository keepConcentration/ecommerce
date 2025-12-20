package com.phm.ecommerce.order.application.usecase.cart;

import com.phm.ecommerce.common.domain.cart.CartItem;
import com.phm.ecommerce.common.infrastructure.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCartItemsUseCase {

  private final CartItemRepository cartItemRepository;

  public record Input(Long userId) {}

  public Output execute(Input input) {
    List<CartItem> cartItems = cartItemRepository.findByUserId(input.userId());

    List<CartItemInfo> cartItemInfos = cartItems.stream()
        .map(cartItem -> new CartItemInfo(
            cartItem.getId(),
            cartItem.getProductId(),
            cartItem.getQuantity()
        ))
        .toList();

    return new Output(cartItemInfos);
  }

  public record Output(List<CartItemInfo> items) {}

  public record CartItemInfo(
      Long cartItemId,
      Long productId,
      Long quantity) {}
}
