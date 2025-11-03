package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddCartItemUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  @Schema(description = "장바구니 상품 추가 요청")
  public record Input(
      @Schema(description = "사용자 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId,

      @Schema(description = "상품 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "상품 ID는 필수입니다")
      Long productId,

      @Schema(description = "수량 (1 이상)", example = "3", requiredMode = RequiredMode.REQUIRED)
      @NotNull(message = "수량은 필수입니다")
      @Min(value = 1, message = "수량은 1 이상이어야 합니다")
      Long quantity) {}

  public Output execute(Input input) {
    Product product = productRepository.findById(input.productId())
        .orElseThrow(ProductNotFoundException::new);

    Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndProductId(
        input.userId(), input.productId());

    CartItem cartItem;
    if (existingCartItem.isPresent()) {
      cartItem = existingCartItem.get();
      cartItem.increaseQuantity(input.quantity());
    } else {
      cartItem = CartItem.create(input.userId(), input.productId(), input.quantity());
    }
    cartItem = cartItemRepository.save(cartItem);

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

      @Schema(description = "수량", example = "3")
      Long quantity) {}
}
