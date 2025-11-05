package com.phm.ecommerce.presentation.mapper;

import com.phm.ecommerce.application.usecase.cart.AddCartItemUseCase;
import com.phm.ecommerce.application.usecase.cart.DeleteCartItemUseCase;
import com.phm.ecommerce.application.usecase.cart.GetCartItemsUseCase;
import com.phm.ecommerce.application.usecase.cart.UpdateCartItemQuantityUseCase;
import com.phm.ecommerce.presentation.dto.request.AddCartItemRequest;
import com.phm.ecommerce.presentation.dto.request.DeleteCartItemRequest;
import com.phm.ecommerce.presentation.dto.request.UpdateQuantityRequest;
import com.phm.ecommerce.presentation.dto.response.CartItemResponse;
import com.phm.ecommerce.presentation.dto.response.CartResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

  public AddCartItemUseCase.Input toInput(AddCartItemRequest request) {
    return new AddCartItemUseCase.Input(
        request.userId(),
        request.productId(),
        request.quantity());
  }

  public CartItemResponse toResponse(AddCartItemUseCase.Output output) {
    return new CartItemResponse(
        output.cartItemId(),
        output.productId(),
        output.productName(),
        output.price(),
        output.quantity());
  }

  public GetCartItemsUseCase.Input toInput(Long userId) {
    return new GetCartItemsUseCase.Input(userId);
  }

  public CartResponse toResponse(GetCartItemsUseCase.Output output) {
    List<CartItemResponse> items = output.items().stream()
        .map(item -> new CartItemResponse(
            item.cartItemId(),
            item.productId(),
            item.productName(),
            item.price(),
            item.quantity()))
        .toList();

    return new CartResponse(items);
  }

  public UpdateCartItemQuantityUseCase.Input toInput(Long cartItemId, UpdateQuantityRequest request) {
    return new UpdateCartItemQuantityUseCase.Input(cartItemId, request.quantity());
  }

  public CartItemResponse toResponse(UpdateCartItemQuantityUseCase.Output output) {
    return new CartItemResponse(
        output.cartItemId(),
        output.productId(),
        output.productName(),
        output.price(),
        output.quantity());
  }

  public DeleteCartItemUseCase.Input toDeleteInput(Long cartItemId, DeleteCartItemRequest request) {
    return new DeleteCartItemUseCase.Input(cartItemId, request.userId());
  }
}
