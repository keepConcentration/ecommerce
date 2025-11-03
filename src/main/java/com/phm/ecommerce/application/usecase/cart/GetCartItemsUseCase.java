package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import com.phm.ecommerce.presentation.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCartItemsUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  public CartResponse execute(Long userId) {
    List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

    List<Long> productIds = cartItems.stream()
        .map(CartItem::getProductId)
        .toList();

    Map<Long, Product> productMap = productRepository.findAllByIds(productIds).stream()
        .collect(Collectors.toMap(Product::getId, product -> product));

    List<CartItemResponse> cartItemResponses = cartItems.stream()
        .map(cartItem -> {
          Product product = productMap.get(cartItem.getProductId());
          if (product == null) {
            throw new ProductNotFoundException();
          }

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
}
