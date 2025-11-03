package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.cart.exception.CartItemNotFoundException;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  @Schema(description = "장바구니 수량 변경 요청")
  public record Input(
      @Schema(description = "장바구니 아이템 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotNull(message = "장바구니 아이템 ID는 필수입니다")
      Long cartItemId,

      @Schema(description = "변경할 수량 (1 이상)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotNull(message = "수량은 필수입니다")
      @Min(value = 1, message = "수량은 1 이상이어야 합니다")
      Long quantity) {}

  public Output execute(Input input) {
    CartItem cartItem = cartItemRepository.findById(input.cartItemId())
        .orElseThrow(CartItemNotFoundException::new);

    cartItem.updateQuantity(input.quantity());
    cartItem = cartItemRepository.save(cartItem);

    Product product = productRepository.findById(cartItem.getProductId())
        .orElseThrow(ProductNotFoundException::new);

    return new Output(
        cartItem.getId(),
        product.getId(),
        product.getName(),
        product.getPrice(),
        cartItem.getQuantity()
    );
  }

  @Schema(description = "장바구니 아이템 정보")
  public record Output(
      @Schema(description = "장바구니 아이템 ID", example = "1")
      Long cartItemId,

      @Schema(description = "상품 ID", example = "1")
      Long productId,

      @Schema(description = "상품명", example = "노트북")
      String productName,

      @Schema(description = "가격", example = "1500000")
      Long price,

      @Schema(description = "수량", example = "5")
      Long quantity) {}
}
