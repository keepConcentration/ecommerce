package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddCartItemUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  public record Input(Long userId, Long productId, Long quantity) {}

  public Output execute(Input input) {
    Product product = productRepository.findById(input.productId())
        .orElseThrow(ProductNotFoundException::new);

    Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndProductId(
        input.userId(), input.productId());

    CartItem cartItem;
    if (existingCartItem.isPresent()) {
      cartItem = existingCartItem.get();
      cartItem.increaseQuantity(input.quantity());
    } else {
      cartItem = CartItem.create(input.userId(), input.productId(), input.quantity());
    }
    cartItem = cartItemRepository.save(cartItem);

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
