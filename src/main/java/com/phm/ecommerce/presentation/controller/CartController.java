package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.cart.AddCartItemUseCase;
import com.phm.ecommerce.application.usecase.cart.DeleteCartItemUseCase;
import com.phm.ecommerce.application.usecase.cart.GetCartItemsUseCase;
import com.phm.ecommerce.application.usecase.cart.UpdateCartItemQuantityUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.CartApi;
import com.phm.ecommerce.presentation.dto.request.AddCartItemRequest;
import com.phm.ecommerce.presentation.dto.request.DeleteCartItemRequest;
import com.phm.ecommerce.presentation.dto.request.UpdateQuantityRequest;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import com.phm.ecommerce.presentation.dto.response.CartResponse;
import com.phm.ecommerce.presentation.mapper.CartMapper;
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
  private final CartMapper cartMapper;

  @Override
  public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(AddCartItemRequest request) {
    AddCartItemUseCase.Output output = addCartItemUseCase.execute(cartMapper.toInput(request));
    CartItemResponse response = cartMapper.toResponse(output);

    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/v1/cart/items/" + output.cartItemId())
        .body(ApiResponse.success(response));
  }

  @Override
  public ApiResponse<CartResponse> getCartItems(Long userId) {
    GetCartItemsUseCase.Output output = getCartItemsUseCase.execute(cartMapper.toInput(userId));
    return ApiResponse.success(cartMapper.toResponse(output));
  }

  @Override
  public ApiResponse<CartItemResponse> updateCartItemQuantity(
      Long cartItemId, UpdateQuantityRequest request) {
    UpdateCartItemQuantityUseCase.Output output = updateCartItemQuantityUseCase.execute(
        cartMapper.toInput(cartItemId, request));
    return ApiResponse.success(cartMapper.toResponse(output));
  }

  @Override
  public ResponseEntity<Void> deleteCartItem(Long cartItemId, DeleteCartItemRequest request) {
    deleteCartItemUseCase.execute(cartMapper.toDeleteInput(cartItemId, request));
    return ResponseEntity.noContent().build();
  }
}
