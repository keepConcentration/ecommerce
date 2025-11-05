package com.phm.ecommerce.application.usecase.order;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.domain.order.OrderPricingService;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.CartItemRepository;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.OrderItemRepository;
import com.phm.ecommerce.persistence.repository.OrderRepository;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final UserCouponRepository userCouponRepository;
  private final CouponRepository couponRepository;
  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderPricingService orderPricingService;

  public record Input(
      Long userId,
      List<CartItemCouponInfo> cartItemCouponMaps) {}

  public record CartItemCouponInfo(
      Long cartItemId,
      Long userCouponId) {}

  public Output execute(Input request) {
    List<CartItem> cartItems = new ArrayList<>();
    for (CartItemCouponInfo map : request.cartItemCouponMaps()) {
      CartItem cartItem = cartItemRepository.findByIdOrThrow(map.cartItemId());

      cartItem.validateOwnership(request.userId());
      cartItems.add(cartItem);
    }

    if (cartItems.isEmpty()) {
      throw new IllegalStateException("주문할 장바구니 아이템이 없습니다");
    }

    Map<Long, Long> cartItemCouponMap = new HashMap<>();
    if (request.cartItemCouponMaps() != null) {
      for (CartItemCouponInfo map : request.cartItemCouponMaps()) {
        cartItemCouponMap.put(map.cartItemId(), map.userCouponId());
      }
    }

    List<OrderItemData> orderItemDataList = new ArrayList<>();
    Long totalAmount = 0L;
    Long totalDiscountAmount = 0L;

    Map<Product, Long> savedProductStockMap = new HashMap<>();
    List<UserCoupon> savedUserCoupons = new ArrayList<>();
    Point savedPoint = null;
    Order savedOrder = null;
    PointTransaction savedPointTransaction = null;
    List<CartItem> removedCartItems = new ArrayList<>();

    try {
      for (CartItem cartItem : cartItems) {
        Product product = productRepository.findByIdOrThrow(cartItem.getProductId());
        product.decreaseStock(cartItem.getQuantity());
        Product savedProduct = productRepository.save(product);
        savedProductStockMap.put(savedProduct, cartItem.getQuantity());

        Long discountAmount = 0L;
        UserCoupon userCoupon = null;
        Long userCouponId = cartItemCouponMap.get(cartItem.getId());

        if (userCouponId != null) {
          userCoupon = userCouponRepository.findByIdOrThrow(userCouponId);
          Coupon coupon = couponRepository.findByIdOrThrow(userCoupon.getCouponId());
          discountAmount = userCoupon.calculateDiscount(coupon);
        }

        orderItemDataList.add(new OrderItemData(cartItem, savedProduct, userCoupon, discountAmount));

        Long itemTotalAmount = orderPricingService.calculateItemTotal(savedProduct, cartItem.getQuantity());
        totalAmount += itemTotalAmount;
        totalDiscountAmount += discountAmount;
      }

      Long finalAmount = orderPricingService.calculateFinalAmount(totalAmount, totalDiscountAmount);

      Point point = pointRepository.findByUserIdOrThrow(request.userId());
      point.deduct(finalAmount);
      savedPoint = pointRepository.save(point);

      Order order = Order.create(request.userId(), totalAmount, totalDiscountAmount);
      savedOrder = orderRepository.save(order);

      List<OrderItemInfo> orderItemInfos = new ArrayList<>();
      for (OrderItemData data : orderItemDataList) {
        OrderItem orderItem =
            OrderItem.create(
                savedOrder.getId(),
                request.userId(),
                data.product.getId(),
                data.product.getName(),
                data.cartItem.getQuantity(),
                data.product.getPrice(),
                data.discountAmount,
                data.userCoupon != null ? data.userCoupon.getId() : null);
        orderItem = orderItemRepository.save(orderItem);

        if (data.userCoupon != null) {
          data.userCoupon.use();
          savedUserCoupons.add(userCouponRepository.save(data.userCoupon));
        }

        orderItemInfos.add(
            new OrderItemInfo(
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getTotalPrice(),
                orderItem.getDiscountAmount(),
                orderItem.getFinalAmount(),
                orderItem.getUserCouponId()));
      }

      PointTransaction pointTransaction =
          PointTransaction.createDeduction(savedPoint.getId(), savedOrder.getId(), finalAmount);
      savedPointTransaction = pointTransactionRepository.save(pointTransaction);

      for (CartItem cartItem : cartItems) {
        removedCartItems.add(cartItem);
        cartItemRepository.deleteById(cartItem.getId());
      }

      return new Output(
          savedOrder.getId(),
          savedOrder.getUserId(),
          savedOrder.getTotalAmount(),
          savedOrder.getDiscountAmount(),
          savedOrder.getFinalAmount(),
          savedOrder.getCreatedAt(),
          orderItemInfos);

    } catch (Exception e) {
      if (!savedProductStockMap.isEmpty()) {
        for (Map.Entry<Product, Long> entry : savedProductStockMap.entrySet()) {
          entry.getKey().increaseStock(entry.getValue());
          try {
            productRepository.save(entry.getKey());
          } catch (Exception rollbackException) {
            // TODO: 로깅 추가
          }
        }
      }

      if (savedPoint != null) {
        Long finalAmount = totalAmount - totalDiscountAmount;
        savedPoint.charge(finalAmount);
        try {
          pointRepository.save(savedPoint);
        } catch (Exception rollbackException) {
          // TODO: 로깅 추가
        }
      }

      if (savedOrder != null) {
        try {
          orderItemRepository.deleteByOrderId(savedOrder.getId());
          orderRepository.deleteById(savedOrder.getId());
        } catch (Exception rollbackException) {
          // TODO: 로깅 추가
        }
      }

      if (!savedUserCoupons.isEmpty()) {
        for (UserCoupon uc : savedUserCoupons) {
          uc.rollbackUsage();
          try {
            userCouponRepository.save(uc);
          } catch (Exception rollbackException) {
            // TODO: 로깅 추가
          }
        }
      }

      if (savedPointTransaction != null) {
        try {
          pointTransactionRepository.deleteById(savedPointTransaction.getId());
        } catch (Exception rollbackException) {
          // TODO: 로깅 추가
        }
      }

      if (!removedCartItems.isEmpty()) {
        for (CartItem cartItem : removedCartItems) {
          try {
            cartItemRepository.save(cartItem);
          } catch (Exception rollbackException) {
            // TODO: 로깅 추가
          }
        }
      }

      throw e;
    }
  }

  public record Output(
      Long orderId,
      Long userId,
      Long totalAmount,
      Long discountAmount,
      Long finalAmount,
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

  // 내부 데이터 클래스
  private static class OrderItemData {
    final CartItem cartItem;
    final Product product;
    final UserCoupon userCoupon;
    final Long discountAmount;

    OrderItemData(CartItem cartItem, Product product, UserCoupon userCoupon, Long discountAmount) {
      this.cartItem = cartItem;
      this.product = product;
      this.userCoupon = userCoupon;
      this.discountAmount = discountAmount;
    }
  }
}
