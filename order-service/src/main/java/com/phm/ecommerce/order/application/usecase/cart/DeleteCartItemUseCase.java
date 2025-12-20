package com.phm.ecommerce.order.application.usecase.cart;

import com.phm.ecommerce.common.domain.cart.exception.CartItemOwnershipViolationException;
import com.phm.ecommerce.common.domain.cart.CartItem;
import com.phm.ecommerce.common.infrastructure.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCartItemUseCase {

  private final CartItemRepository cartItemRepository;

  public record Input(Long cartItemId, Long userId) {}

  public void execute(Input input) {
    log.info("장바구니 아이템 삭제 시작 - cartItemId: {}, userId: {}", input.cartItemId(), input.userId());

    CartItem cartItem = cartItemRepository.findByIdOrThrow(input.cartItemId());
    if (!cartItem.getUserId().equals(input.userId())) {
      log.warn("장바구니 아이템 삭제 실패 - 소유권 위반. cartItemId: {}, requestUserId: {}, ownerUserId: {}",
          input.cartItemId(), input.userId(), cartItem.getUserId());
      throw new CartItemOwnershipViolationException();
    }

    cartItemRepository.deleteById(cartItem.getId());
    log.info("장바구니 아이템 삭제 완료 - cartItemId: {}", input.cartItemId());
  }
}
