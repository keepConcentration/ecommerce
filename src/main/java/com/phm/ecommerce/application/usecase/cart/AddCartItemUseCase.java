package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.presentation.dto.request.AddCartItemRequest;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddCartItemUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  public CartItemResponse execute(AddCartItemRequest request) {
    Product product = productRepository.findById(request.productId())
        .orElseThrow(ProductNotFoundException::new);

    Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndProductId(
        request.userId(), request.productId());

    CartItem cartItem;
    if (existingCartItem.isPresent()) {
      cartItem = existingCartItem.get();
      cartItem.increaseQuantity(request.quantity());
    } else {
      cartItem = CartItem.create(request.userId(), request.productId(), request.quantity());
    }
    cartItem = cartItemRepository.save(cartItem);

    return new CartItemResponse(
        cartItem.getId(),
        product.getId(),
        product.getName(),
        product.getPrice(),
        cartItem.getQuantity()
    );
  }
}
