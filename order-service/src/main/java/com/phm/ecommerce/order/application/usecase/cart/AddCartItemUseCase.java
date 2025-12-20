package com.phm.ecommerce.order.application.usecase.cart;

import com.phm.ecommerce.common.domain.cart.CartItem;
import com.phm.ecommerce.common.infrastructure.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddCartItemUseCase {

  private final CartItemRepository cartItemRepository;

  public record Input(Long userId, Long productId, Long quantity) {}

  public Output execute(Input input) {
    log.info("장바구니 아이템 추가 시작 - userId: {}, productId: {}, quantity: {}",
        input.userId(), input.productId(), input.quantity());

    Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndProductId(
        input.userId(), input.productId());

    CartItem cartItem;
    if (existingCartItem.isPresent()) {
      cartItem = existingCartItem.get();
      cartItem.increaseQuantity(input.quantity());
      log.debug("기존 장바구니 아이템 수량 증가 - cartItemId: {}, newQuantity: {}",
          cartItem.getId(), cartItem.getQuantity());
    } else {
      cartItem = CartItem.create(input.userId(), input.productId(), input.quantity());
      log.debug("새로운 장바구니 아이템 생성 - userId: {}, productId: {}",
          input.userId(), input.productId());
    }
    cartItem = cartItemRepository.save(cartItem);

    log.info("장바구니 아이템 추가 완료 - cartItemId: {}, totalQuantity: {}",
        cartItem.getId(), cartItem.getQuantity());

    return new Output(
        cartItem.getId(),
        cartItem.getProductId(),
        cartItem.getQuantity()
    );
  }

  public record Output(
      Long cartItemId,
      Long productId,
      Long quantity) {}
}
