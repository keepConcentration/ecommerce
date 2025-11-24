package com.phm.ecommerce.presentation.controller;

import com.phm.ecommerce.application.usecase.order.CreateDirectOrderUseCase;
import com.phm.ecommerce.application.usecase.order.CreateOrderUseCase;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.controller.api.OrderApi;
import com.phm.ecommerce.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.presentation.dto.response.OrderResponse;
import com.phm.ecommerce.presentation.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

  private final CreateOrderUseCase createOrderUseCase;
  private final CreateDirectOrderUseCase createDirectOrderUseCase;
  private final OrderMapper orderMapper;

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest request) {
    CreateOrderUseCase.Output output = createOrderUseCase.execute(orderMapper.toInput(request));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(orderMapper.toResponse(output)));
  }

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createDirectOrder(DirectOrderRequest request) {
    CreateDirectOrderUseCase.Output output = createDirectOrderUseCase.execute(orderMapper.toInput(request));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(orderMapper.toResponse(output)));
  }
}
