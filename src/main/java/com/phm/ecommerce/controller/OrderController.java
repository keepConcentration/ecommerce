package com.phm.ecommerce.controller;

import com.phm.ecommerce.common.ApiResponse;
import com.phm.ecommerce.controller.api.OrderApi;
import com.phm.ecommerce.dto.request.CreateOrderRequest;
import com.phm.ecommerce.dto.response.OrderResponse;
import com.phm.ecommerce.dto.response.OrderItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderApi {

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest request) {
    List<OrderItemResponse> orderItems =
        List.of(
            OrderItemResponse.builder()
                .orderItemId(1L)
                .productId(1L)
                .productName("노트북")
                .quantity(2L)
                .price(1500000L)
                .totalPrice(3000000L)
                .discountAmount(50000L)
                .finalAmount(2950000L)
                .userCouponId(10L)
                .build(),
            OrderItemResponse.builder()
                .orderItemId(2L)
                .productId(2L)
                .productName("마우스")
                .quantity(1L)
                .price(35000L)
                .totalPrice(35000L)
                .discountAmount(5000L)
                .finalAmount(30000L)
                .userCouponId(11L)
                .build());
    OrderResponse order = OrderResponse.builder().orderItems(orderItems).build();
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
  }
}
