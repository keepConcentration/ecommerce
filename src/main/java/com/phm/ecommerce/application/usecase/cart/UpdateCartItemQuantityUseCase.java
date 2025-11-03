package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.cart.exception.CartItemNotFoundException;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  public CartItemResponse execute(Long cartItemId, Long newQuantity) {
    CartItem cartItem = cartItemRepository.findById(cartItemId)
        .orElseThrow(CartItemNotFoundException::new);

    cartItem.updateQuantity(newQuantity);
    cartItem = cartItemRepository.save(cartItem);

    Product product = productRepository.findById(cartItem.getProductId())
        .orElseThrow(ProductNotFoundException::new);

    return new CartItemResponse(
        cartItem.getId(),
        product.getId(),
        product.getName(),
        product.getPrice(),
        cartItem.getQuantity()
    );
  }
}
