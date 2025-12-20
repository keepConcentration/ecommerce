package com.phm.ecommerce.order.presentation.mapper;

import com.phm.ecommerce.order.application.usecase.order.CreateOrderSagaUseCase;
import com.phm.ecommerce.order.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.order.presentation.dto.response.OrderItemResponse;
import com.phm.ecommerce.order.presentation.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

  public CreateOrderSagaUseCase.Input toInput(CreateOrderRequest request) {
    List<CreateOrderSagaUseCase.CartItemCouponInfo> cartItemCouponMaps = request.cartItemCouponMaps().stream()
        .map(map -> new CreateOrderSagaUseCase.CartItemCouponInfo(
            map.cartItemId(),
            map.userCouponId()))
        .toList();

    return new CreateOrderSagaUseCase.Input(request.userId(), cartItemCouponMaps);
  }

  public OrderResponse toResponse(CreateOrderSagaUseCase.Output output) {
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
