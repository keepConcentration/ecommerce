package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.cart.AddCartItemUseCase;
import com.phm.ecommerce.application.usecase.cart.DeleteCartItemUseCase;
import com.phm.ecommerce.application.usecase.cart.GetCartItemsUseCase;
import com.phm.ecommerce.application.usecase.cart.UpdateCartItemQuantityUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.CartApi;
import com.phm.ecommerce.presentation.dto.request.AddCartItemRequest;
import com.phm.ecommerce.presentation.dto.request.UpdateQuantityRequest;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import com.phm.ecommerce.presentation.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {

  private final AddCartItemUseCase addCartItemUseCase;
  private final GetCartItemsUseCase getCartItemsUseCase;
  private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
  private final DeleteCartItemUseCase deleteCartItemUseCase;

  @Override
  public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(AddCartItemRequest request) {
    CartItemResponse cartItem = addCartItemUseCase.execute(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/v1/cart/items/" + cartItem.cartItemId())
        .body(ApiResponse.success(cartItem));
  }

  @Override
  public ApiResponse<CartResponse> getCartItems(Long userId) {
    CartResponse cart = getCartItemsUseCase.execute(userId);
    return ApiResponse.success(cart);
  }

  @Override
  public ApiResponse<CartItemResponse> updateCartItemQuantity(
      Long cartItemId, UpdateQuantityRequest request) {
    CartItemResponse cartItem = updateCartItemQuantityUseCase.execute(cartItemId, request.quantity());
    return ApiResponse.success(cartItem);
  }

  @Override
  public ResponseEntity<Void> deleteCartItem(Long cartItemId) {
    deleteCartItemUseCase.execute(cartItemId);
    return ResponseEntity.noContent().build();
  }
}
