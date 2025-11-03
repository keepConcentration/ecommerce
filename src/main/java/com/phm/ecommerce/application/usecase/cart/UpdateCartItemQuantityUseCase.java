package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  public record Input(Long cartItemId, Long quantity) {}

  public Output execute(Input input) {
    CartItem cartItem = cartItemRepository.findByIdOrThrow(input.cartItemId());

    cartItem.updateQuantity(input.quantity());
    cartItem = cartItemRepository.save(cartItem);

    Product product = productRepository.findByIdOrThrow(cartItem.getProductId());

    return new Output(
        cartItem.getId(),
        product.getId(),
        product.getName(),
        product.getPrice(),
        cartItem.getQuantity()
    );
  }

  public record Output(
      Long cartItemId,
      Long productId,
      String productName,
      Long price,
      Long quantity) {}
}
