package com.phm.ecommerce.presentation.mapper;

import com.phm.ecommerce.application.usecase.order.CreateDirectOrderUseCase;
import com.phm.ecommerce.application.usecase.order.CreateOrderUseCase;
import com.phm.ecommerce.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.presentation.dto.response.OrderItemResponse;
import com.phm.ecommerce.presentation.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

  public CreateOrderUseCase.Input toInput(CreateOrderRequest request) {
    List<CreateOrderUseCase.CartItemCouponInfo> cartItemCouponMaps = request.cartItemCouponMaps().stream()
        .map(map -> new CreateOrderUseCase.CartItemCouponInfo(
            map.cartItemId(),
            map.userCouponId()))
        .toList();

    return new CreateOrderUseCase.Input(request.userId(), cartItemCouponMaps);
  }

  public OrderResponse toResponse(CreateOrderUseCase.Output output) {
    List<OrderItemResponse> orderItems = output.orderItems().stream()
        .map(item -> new OrderItemResponse(
            item.orderItemId(),
            item.productId(),
            item.productName(),
            item.quantity(),
            item.price(),
            item.totalPrice(),
            item.discountAmount(),
            item.finalAmount(),
            item.userCouponId()))
        .toList();

    return new OrderResponse(
        output.orderId(),
        output.userId(),
        output.totalAmount(),
        output.discountAmount(),
        output.finalAmount(),
        output.createdAt(),
        orderItems);
  }

  public CreateDirectOrderUseCase.Input toInput(DirectOrderRequest request) {
    return new CreateDirectOrderUseCase.Input(
        request.userId(),
        request.productId(),
        request.quantity(),
        request.userCouponId());
  }

  public OrderResponse toResponse(CreateDirectOrderUseCase.Output output) {
    List<OrderItemResponse> orderItems = output.orderItems().stream()
        .map(item -> new OrderItemResponse(
            item.orderItemId(),
            item.productId(),
            item.productName(),
            item.quantity(),
            item.price(),
            item.totalPrice(),
            item.discountAmount(),
            item.finalAmount(),
            item.userCouponId()))
        .toList();

    return new OrderResponse(
        output.orderId(),
        output.userId(),
        output.totalAmount(),
        output.discountAmount(),
        output.finalAmount(),
        output.createdAt(),
        orderItems);
  }
}
