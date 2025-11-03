package com.phm.ecommerce.application.usecase.cart;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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

  @Schema(description = "장바구니 조회 요청")
  public record Input(
      @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId) {}

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

  @Schema(description = "장바구니 정보")
  public record Output(
      @Schema(description = "장바구니 아이템 목록")
      List<CartItemInfo> items) {}

  @Schema(description = "장바구니 아이템 정보")
  public record CartItemInfo(
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
