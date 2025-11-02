package com.phm.ecommerce.application.service;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.cart.exception.CartItemNotFoundException;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.presentation.dto.request.AddCartItemRequest;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import com.phm.ecommerce.presentation.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  public CartItemResponse addCartItem(AddCartItemRequest request) {
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

  public CartResponse getCartItems(Long userId) {
    List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

    // TODO productIds 로 product 조회 기능 추가
    List<CartItemResponse> cartItemResponses = cartItems.stream()
        .map(cartItem -> {
          Product product = productRepository.findById(cartItem.getProductId())
              .orElseThrow(ProductNotFoundException::new);

          return new CartItemResponse(
              cartItem.getId(),
              product.getId(),
              product.getName(),
              product.getPrice(),
              cartItem.getQuantity()
          );
        })
        .toList();

    return new CartResponse(cartItemResponses);
  }

  public CartItemResponse updateCartItemQuantity(Long cartItemId, Long newQuantity) {
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

  public void deleteCartItem(Long cartItemId) {
    CartItem cartItem = cartItemRepository.findById(cartItemId)
        .orElseThrow(CartItemNotFoundException::new);

    cartItemRepository.deleteById(cartItem.getId());
  }
}
