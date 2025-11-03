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

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {

  private final AddCartItemUseCase addCartItemUseCase;
  private final GetCartItemsUseCase getCartItemsUseCase;
  private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
  private final DeleteCartItemUseCase deleteCartItemUseCase;

  @Override
  public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(AddCartItemRequest request) {
    AddCartItemUseCase.Input input = new AddCartItemUseCase.Input(
        request.userId(), request.productId(), request.quantity());
    AddCartItemUseCase.Output output = addCartItemUseCase.execute(input);

    CartItemResponse response = new CartItemResponse(
        output.cartItemId(), output.productId(), output.productName(),
        output.price(), output.quantity());

    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/v1/cart/items/" + output.cartItemId())
        .body(ApiResponse.success(response));
  }

  @Override
  public ApiResponse<CartResponse> getCartItems(Long userId) {
    GetCartItemsUseCase.Output output = getCartItemsUseCase.execute(
        new GetCartItemsUseCase.Input(userId));

    List<CartItemResponse> items = output.items().stream()
        .map(item -> new CartItemResponse(item.cartItemId(), item.productId(),
            item.productName(), item.price(), item.quantity()))
        .toList();

    CartResponse response = new CartResponse(items);
    return ApiResponse.success(response);
  }

  @Override
  public ApiResponse<CartItemResponse> updateCartItemQuantity(
      Long cartItemId, UpdateQuantityRequest request) {
    UpdateCartItemQuantityUseCase.Output output = updateCartItemQuantityUseCase.execute(
        new UpdateCartItemQuantityUseCase.Input(cartItemId, request.quantity()));

    CartItemResponse response = new CartItemResponse(
        output.cartItemId(), output.productId(), output.productName(),
        output.price(), output.quantity());

    return ApiResponse.success(response);
  }

  @Override
  public ResponseEntity<Void> deleteCartItem(Long cartItemId) {
    deleteCartItemUseCase.execute(new DeleteCartItemUseCase.Input(cartItemId));
    return ResponseEntity.noContent().build();
  }
}
