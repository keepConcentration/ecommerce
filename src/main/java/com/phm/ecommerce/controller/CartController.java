package com.phm.ecommerce.controller;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.controller.api.CartApi;
import com.phm.ecommerce.dto.request.AddCartItemRequest;
import com.phm.ecommerce.dto.request.UpdateQuantityRequest;
import com.phm.ecommerce.dto.response.CartResponse;
import com.phm.ecommerce.dto.response.CartItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController implements CartApi {

  @Override
  public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(AddCartItemRequest request) {
    CartItemResponse cartItem =
        CartItemResponse.builder()
            .cartItemId(1L)
            .productId(request.getProductId())
            .productName("노트북")
            .price(1500000L)
            .quantity(request.getQuantity())
            .build();
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/v1/cart/items/1")
        .body(ApiResponse.success(cartItem));
  }

  @Override
  public ApiResponse<CartResponse> getCartItems(Long userId) {
    List<CartItemResponse> items =
        List.of(
            CartItemResponse.builder()
                .cartItemId(1L)
                .productId(1L)
                .productName("노트북")
                .price(1500000L)
                .quantity(2L)
                .build(),
            CartItemResponse.builder()
                .cartItemId(2L)
                .productId(2L)
                .productName("마우스")
                .price(35000L)
                .quantity(1L)
                .build());
    CartResponse cart = CartResponse.builder().items(items).build();
    return ApiResponse.success(cart);
  }

  @Override
  public ApiResponse<CartItemResponse> updateCartItemQuantity(
      Long cartItemId, UpdateQuantityRequest request) {
    CartItemResponse cartItem =
        CartItemResponse.builder()
            .cartItemId(cartItemId)
            .productId(1L)
            .productName("노트북")
            .price(1500000L)
            .quantity(request.getQuantity())
            .build();
    return ApiResponse.success(cartItem);
  }

  @Override
  public ResponseEntity<Void> deleteCartItem(Long cartItemId) {
    return ResponseEntity.noContent().build();
  }
}
