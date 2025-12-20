package com.phm.ecommerce.order.application.usecase.order;

import com.phm.ecommerce.order.infrastructure.outbox.OutboxService;
import com.phm.ecommerce.common.domain.cart.CartItem;
import com.phm.ecommerce.common.domain.order.Order;
import com.phm.ecommerce.common.event.order.OrderCreatedEvent;
import com.phm.ecommerce.common.domain.order.OrderItem;
import com.phm.ecommerce.common.domain.order.exception.EmptyCartException;
import com.phm.ecommerce.common.infrastructure.repository.CartItemRepository;
import com.phm.ecommerce.common.infrastructure.repository.OrderItemRepository;
import com.phm.ecommerce.common.infrastructure.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CreateOrderUseCase refactored for Choreography Saga pattern.
 *
 * Saga Flow:
 * 1. Order Service: Create order with PENDING status
 * 2. Publish OrderCreatedEvent via Outbox
 * 3. Product Service listens and reserves stock
 * 4. Promotion Service listens and reserves coupons
 * 5. Payment Service listens and deducts points
 * 6. Order Service listens to PaymentCompleted and marks order as COMPLETED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderSagaUseCase {

  private final CartItemRepository cartItemRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OutboxService outboxService;

  public record Input(
      Long userId,
      List<CartItemCouponInfo> cartItemCouponMaps) {}

  public record CartItemCouponInfo(
      Long cartItemId,
      Long userCouponId) {}

  /**
   * Create order and publish OrderCreatedEvent.
   * The saga will be completed asynchronously by event listeners.
   */
  @Transactional
  public Output execute(Input request) {
    log.info("주문 생성 시작 (Saga) - userId: {}, cartItemCount: {}",
        request.userId(), request.cartItemCouponMaps().size());

    // 1. Validate and load cart items
    List<CartItem> cartItems = new ArrayList<>();
    Map<Long, Long> cartItemCouponMap = new HashMap<>();

    for (CartItemCouponInfo map : request.cartItemCouponMaps()) {
      CartItem cartItem = cartItemRepository.findByIdOrThrow(map.cartItemId());
      cartItem.validateOwnership(request.userId());
      cartItems.add(cartItem);

      if (map.userCouponId() != null) {
        cartItemCouponMap.put(map.cartItemId(), map.userCouponId());
      }
    }

    if (cartItems.isEmpty()) {
      log.warn("주문 실패 - 장바구니가 비어있음. userId: {}", request.userId());
      throw new EmptyCartException();
    }

    // 2. Calculate order amounts (without calling other services)
    Long totalAmount = 0L;
    Long estimatedDiscountAmount = 0L;  // Actual discount calculated by Promotion Service
    List<OrderItemData> orderItemDataList = new ArrayList<>();

    for (CartItem cartItem : cartItems) {
      Long itemTotal = cartItem.getQuantity() * 10000L;  // Price will be verified by Product Service
      totalAmount += itemTotal;

      // Estimated discount (will be finalized by Promotion Service)
      Long userCouponId = cartItemCouponMap.get(cartItem.getId());
      Long estimatedDiscount = 0L;
      if (userCouponId != null) {
        estimatedDiscount = 1000L;  // Placeholder, actual calculated by Promotion Service
        estimatedDiscountAmount += estimatedDiscount;
      }

      orderItemDataList.add(new OrderItemData(cartItem, userCouponId, estimatedDiscount));
    }

    Long finalAmount = totalAmount - estimatedDiscountAmount;

    // 3. Create Order entity with PENDING status
    Order order = Order.create(request.userId(), totalAmount, estimatedDiscountAmount);
    order = orderRepository.save(order);

    // 4. Create OrderItems
    List<OrderItemInfo> orderItemInfos = new ArrayList<>();
    for (OrderItemData data : orderItemDataList) {
      OrderItem orderItem = OrderItem.create(
          order.getId(),
          request.userId(),
          data.cartItem.getProductId(),
          "상품명 미정",  // Will be updated by Product Service
          data.cartItem.getQuantity(),
          10000L,  // Placeholder price
          data.estimatedDiscount,
          data.userCouponId
      );
      orderItem = orderItemRepository.save(orderItem);

      orderItemInfos.add(new OrderItemInfo(
          orderItem.getId(),
          orderItem.getProductId(),
          orderItem.getProductName(),
          orderItem.getQuantity(),
          orderItem.getPrice(),
          orderItem.getTotalPrice(),
          orderItem.getDiscountAmount(),
          orderItem.getFinalAmount(),
          orderItem.getUserCouponId()
      ));
    }

    // 5. Publish OrderCreatedEvent via Outbox Pattern
    List<OrderCreatedEvent.OrderItemInfo> eventItems = orderItemInfos.stream()
        .map(item -> new OrderCreatedEvent.OrderItemInfo(
            item.productId(),
            item.quantity(),
            item.price(),
            item.userCouponId()
        ))
        .toList();

    OrderCreatedEvent event = OrderCreatedEvent.create(
        order.getId().toString(),
        order.getUserId(),
        eventItems,
        order.getTotalAmount(),
        order.getDiscountAmount(),
        order.getFinalAmount()
    );

    outboxService.publish("ORDER", event);

    log.info("주문 생성 완료 (PENDING) - orderId: {}, userId: {}, finalAmount: {}",
        order.getId(), order.getUserId(), order.getFinalAmount());

    return new Output(
        order.getId(),
        order.getUserId(),
        order.getTotalAmount(),
        order.getDiscountAmount(),
        order.getFinalAmount(),
        order.getStatus().name(),
        order.getCreatedAt(),
        orderItemInfos
    );
  }

  public record Output(
      Long orderId,
      Long userId,
      Long totalAmount,
      Long discountAmount,
      Long finalAmount,
      String status,
      LocalDateTime createdAt,
      List<OrderItemInfo> orderItems) {}

  public record OrderItemInfo(
      Long orderItemId,
      Long productId,
      String productName,
      Long quantity,
      Long price,
      Long totalPrice,
      Long discountAmount,
      Long finalAmount,
      Long userCouponId) {}

  private record OrderItemData(
      CartItem cartItem,
      Long userCouponId,
      Long estimatedDiscount) {}
}
