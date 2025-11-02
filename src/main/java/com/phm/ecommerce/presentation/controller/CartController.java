package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.service.CartService;
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

  private final CartService cartService;

  @Override
  public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(AddCartItemRequest request) {
    CartItemResponse cartItem = cartService.addCartItem(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/v1/cart/items/" + cartItem.cartItemId())
        .body(ApiResponse.success(cartItem));
  }

  @Override
  public ApiResponse<CartResponse> getCartItems(Long userId) {
    CartResponse cart = cartService.getCartItems(userId);
    return ApiResponse.success(cart);
  }

  @Override
  public ApiResponse<CartItemResponse> updateCartItemQuantity(
      Long cartItemId, UpdateQuantityRequest request) {
    CartItemResponse cartItem = cartService.updateCartItemQuantity(cartItemId, request.quantity());
    return ApiResponse.success(cartItem);
  }

  @Override
  public ResponseEntity<Void> deleteCartItem(Long cartItemId) {
    cartService.deleteCartItem(cartItemId);
    return ResponseEntity.noContent().build();
  }
}
