package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.order.CreateDirectOrderUseCase;
import com.phm.ecommerce.application.usecase.order.CreateOrderUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.OrderApi;
import com.phm.ecommerce.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

  private final CreateOrderUseCase createOrderUseCase;
  private final CreateDirectOrderUseCase createDirectOrderUseCase;

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest request) {
    OrderResponse order = createOrderUseCase.execute(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
  }

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createDirectOrder(DirectOrderRequest request) {
    OrderResponse order = createDirectOrderUseCase.execute(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
  }
}
