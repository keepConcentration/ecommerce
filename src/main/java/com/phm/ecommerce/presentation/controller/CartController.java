package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.CartApi;
import com.phm.ecommerce.presentation.dto.request.AddCartItemRequest;
import com.phm.ecommerce.presentation.dto.request.UpdateQuantityRequest;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import com.phm.ecommerce.presentation.dto.response.CartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CartController implements CartApi {

  @Override
  public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(AddCartItemRequest request) {
    CartItemResponse cartItem = new CartItemResponse(1L, request.productId(), "노트북", 1500000L, request.quantity());
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/v1/cart/items/1")
        .body(ApiResponse.success(cartItem));
  }

  @Override
  public ApiResponse<CartResponse> getCartItems(Long userId) {
    List<CartItemResponse> items =
        List.of(
            new CartItemResponse(1L, 1L, "노트북", 1500000L, 2L),
            new CartItemResponse(2L, 2L, "마우스", 35000L, 1L));
    CartResponse cart = new CartResponse(items);
    return ApiResponse.success(cart);
  }

  @Override
  public ApiResponse<CartItemResponse> updateCartItemQuantity(
      Long cartItemId, UpdateQuantityRequest request) {
    CartItemResponse cartItem = new CartItemResponse(cartItemId, 1L, "노트북", 1500000L, request.quantity());
    return ApiResponse.success(cartItem);
  }

  @Override
  public ResponseEntity<Void> deleteCartItem(Long cartItemId) {
    return ResponseEntity.noContent().build();
  }
}
