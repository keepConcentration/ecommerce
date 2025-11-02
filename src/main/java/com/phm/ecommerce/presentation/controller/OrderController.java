package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.OrderApi;
import com.phm.ecommerce.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.presentation.dto.response.OrderItemResponse;
import com.phm.ecommerce.presentation.dto.response.OrderResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController implements OrderApi {

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest request) {
    List<OrderItemResponse> orderItems =
        List.of(
            new OrderItemResponse(1L, 1L, "노트북", 2L, 1500000L, 3000000L, 50000L, 2950000L, 10L),
            new OrderItemResponse(2L, 2L, "마우스", 1L, 35000L, 35000L, 5000L, 30000L, 11L));
    Long totalPrice = 1535000L;
    Long discountAmount = 55000L;
    Long finalAmount = totalPrice - discountAmount;

    OrderResponse order = new OrderResponse(1L, request.userId(), totalPrice, discountAmount, finalAmount, LocalDateTime.of(2025, 11, 1, 10, 30), orderItems);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
  }

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createDirectOrder(DirectOrderRequest request) {
    Long totalPrice = 1500000L * request.quantity();
    Long discountAmount = request.userCouponId() != null ? 50000L : 0L;
    Long finalAmount = totalPrice - discountAmount;

    OrderItemResponse orderItem = new OrderItemResponse(1L, request.productId(), "노트북", request.quantity(), 1500000L, totalPrice, discountAmount, finalAmount, request.userCouponId());
    OrderResponse order = new OrderResponse(1L, request.userId(), totalPrice, discountAmount, finalAmount, LocalDateTime.of(2025, 11, 1, 10, 30), List.of(orderItem));
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
  }
}
