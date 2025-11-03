package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.order.CreateDirectOrderUseCase;
import com.phm.ecommerce.application.usecase.order.CreateOrderUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.OrderApi;
import com.phm.ecommerce.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.presentation.dto.response.OrderItemResponse;
import com.phm.ecommerce.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

  private final CreateOrderUseCase createOrderUseCase;
  private final CreateDirectOrderUseCase createDirectOrderUseCase;

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest request) {
    CreateOrderUseCase.Input input = new CreateOrderUseCase.Input(
        request.userId(),
        request.cartItemCouponMaps().stream()
            .map(map -> new CreateOrderUseCase.CartItemCouponInfo(map.cartItemId(), map.userCouponId()))
            .toList());
    CreateOrderUseCase.Output output = createOrderUseCase.execute(input);

    List<OrderItemResponse> orderItemResponses = output.orderItems().stream()
        .map(item -> new OrderItemResponse(item.orderItemId(), item.productId(), item.productName(),
            item.quantity(), item.price(), item.totalPrice(), item.discountAmount(),
            item.finalAmount(), item.userCouponId()))
        .toList();

    OrderResponse order = new OrderResponse(output.orderId(), output.userId(), output.totalAmount(),
        output.discountAmount(), output.finalAmount(), output.createdAt(), orderItemResponses);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
  }

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createDirectOrder(DirectOrderRequest request) {
    CreateDirectOrderUseCase.Input input = new CreateDirectOrderUseCase.Input(
        request.userId(), request.productId(), request.quantity(), request.userCouponId());
    CreateDirectOrderUseCase.Output output = createDirectOrderUseCase.execute(input);

    List<OrderItemResponse> orderItemResponses = output.orderItems().stream()
        .map(item -> new OrderItemResponse(item.orderItemId(), item.productId(), item.productName(),
            item.quantity(), item.price(), item.totalPrice(), item.discountAmount(),
            item.finalAmount(), item.userCouponId()))
        .toList();

    OrderResponse order = new OrderResponse(output.orderId(), output.userId(), output.totalAmount(),
        output.discountAmount(), output.finalAmount(), output.createdAt(), orderItemResponses);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
  }
}
