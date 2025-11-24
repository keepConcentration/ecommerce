package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.infrastructure.repository.CartItemRepository;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
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

  public record Input(Long userId) {}

  public Output execute(Input input) {
    List<CartItem> cartItems = cartItemRepository.findByUserId(input.userId());

    List<Long> productIds = cartItems.stream()
        .map(CartItem::getProductId)
        .toList();

    Map<Long, Product> productMap = productRepository.findAllByIds(productIds).stream()
        .collect(Collectors.toMap(Product::getId, product -> product));

    List<CartItemInfo> cartItemInfos = cartItems.stream()
        .map(cartItem -> {
          Product product = productMap.get(cartItem.getProductId());
          if (product == null) {
            throw new ProductNotFoundException();
          }

          return new CartItemInfo(
              cartItem.getId(),
              product.getId(),
              product.getName(),
              product.getPrice(),
              cartItem.getQuantity()
          );
        })
        .toList();

    return new Output(cartItemInfos);
  }

  public record Output(List<CartItemInfo> items) {}

  public record CartItemInfo(
      Long cartItemId,
      Long productId,
      String productName,
      Long price,
      Long quantity) {}
}
