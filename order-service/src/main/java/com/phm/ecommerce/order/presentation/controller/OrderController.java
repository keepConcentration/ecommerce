package com.phm.ecommerce.order.presentation.controller;

import com.phm.ecommerce.order.application.usecase.order.CreateOrderSagaUseCase;
import com.phm.ecommerce.order.presentation.common.ApiResponse;
import com.phm.ecommerce.order.presentation.controller.api.OrderApi;
import com.phm.ecommerce.order.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.order.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.order.presentation.dto.response.OrderResponse;
import com.phm.ecommerce.order.presentation.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

  private final CreateOrderSagaUseCase createOrderSagaUseCase;
  private final OrderMapper orderMapper;

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest request) {
    CreateOrderSagaUseCase.Output output = createOrderSagaUseCase.execute(orderMapper.toInput(request));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(orderMapper.toResponse(output)));
  }

  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createDirectOrder(DirectOrderRequest request) {
    // Direct order not supported in Saga pattern yet
    // TODO: Implement CreateDirectOrderSagaUseCase if needed
    throw new UnsupportedOperationException("Direct order is not supported in Saga pattern yet");
  }
}
