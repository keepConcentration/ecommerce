package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  public record Input(Long cartItemId, Long quantity) {}

  public Output execute(Input input) {
    log.info("장바구니 아이템 수량 변경 시작 - cartItemId: {}, newQuantity: {}",
        input.cartItemId(), input.quantity());

    CartItem cartItem = cartItemRepository.findByIdOrThrow(input.cartItemId());

    cartItem.updateQuantity(input.quantity());
    cartItem = cartItemRepository.save(cartItem);

    Product product = productRepository.findByIdOrThrow(cartItem.getProductId());

    log.info("장바구니 아이템 수량 변경 완료 - cartItemId: {}, quantity: {}",
        cartItem.getId(), cartItem.getQuantity());

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
