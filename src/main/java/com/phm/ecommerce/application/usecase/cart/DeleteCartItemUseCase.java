package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.cart.exception.CartItemNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCartItemUseCase {

  private final CartItemRepository cartItemRepository;

  @Schema(description = "장바구니 아이템 삭제 요청")
  public record Input(
      @Schema(description = "장바구니 아이템 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotNull(message = "장바구니 아이템 ID는 필수입니다")
      Long cartItemId) {}

  public void execute(Input input) {
    CartItem cartItem = cartItemRepository.findById(input.cartItemId())
        .orElseThrow(CartItemNotFoundException::new);

    cartItemRepository.deleteById(cartItem.getId());
  }
}
