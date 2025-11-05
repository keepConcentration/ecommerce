package com.phm.ecommerce.application.usecase.order;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.domain.order.OrderPricingService;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.OrderItemRepository;
import com.phm.ecommerce.persistence.repository.OrderRepository;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateDirectOrderUseCase {

  private final ProductRepository productRepository;
  private final UserCouponRepository userCouponRepository;
  private final CouponRepository couponRepository;
  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderPricingService orderPricingService;

  public record Input(Long userId, Long productId, Long quantity, Long userCouponId) {}

  public Output execute(Input request) {
    Product savedProduct = null;
    UserCoupon savedUserCoupon = null;
    Point savedPoint = null;
    Order savedOrder = null;
    PointTransaction savedPointTransaction = null;
    Long finalAmount = 0L;

    try {
      Product product = productRepository.findByIdOrThrow(request.productId());
      product.decreaseStock(request.quantity());
      savedProduct = productRepository.save(product);

      UserCoupon userCoupon = null;
      Long discountAmount = 0L;
      if (request.userCouponId() != null) {
        userCoupon = userCouponRepository.findByIdOrThrow(request.userCouponId());
        Coupon coupon = couponRepository.findByIdOrThrow(userCoupon.getCouponId());
        discountAmount = userCoupon.calculateDiscount(coupon);
      }

      Long totalAmount = orderPricingService.calculateItemTotal(savedProduct, request.quantity());
      finalAmount = orderPricingService.calculateFinalAmount(totalAmount, discountAmount);

      Point point = pointRepository.findByUserIdOrThrow(request.userId());
      point.deduct(finalAmount);
      savedPoint = pointRepository.save(point);

      Order order = Order.create(request.userId(), totalAmount, discountAmount);
      savedOrder = orderRepository.save(order);

      OrderItem orderItem =
          OrderItem.create(
              savedOrder.getId(),
              request.userId(),
              request.productId(),
              savedProduct.getName(),
              request.quantity(),
              savedProduct.getPrice(),
              discountAmount,
              request.userCouponId());
      orderItem = orderItemRepository.save(orderItem);

      if (userCoupon != null) {
        userCoupon.use();
        savedUserCoupon = userCouponRepository.save(userCoupon);
      }

      PointTransaction pointTransaction =
          PointTransaction.createDeduction(savedPoint.getId(), savedOrder.getId(), finalAmount);
      savedPointTransaction = pointTransactionRepository.save(pointTransaction);

      OrderItemInfo orderItemInfo =
          new OrderItemInfo(
              orderItem.getId(),
              orderItem.getProductId(),
              orderItem.getProductName(),
              orderItem.getQuantity(),
              orderItem.getPrice(),
              orderItem.getTotalPrice(),
              orderItem.getDiscountAmount(),
              orderItem.getFinalAmount(),
              orderItem.getUserCouponId());

      return new Output(
          savedOrder.getId(),
          savedOrder.getUserId(),
          savedOrder.getTotalAmount(),
          savedOrder.getDiscountAmount(),
          savedOrder.getFinalAmount(),
          savedOrder.getCreatedAt(),
          List.of(orderItemInfo));

    } catch (Exception e) {
      if (savedProduct != null) {
        savedProduct.increaseStock(request.quantity());
        try {
          productRepository.save(savedProduct);
        } catch (Exception rollbackException) {

        }
      }

      if (savedPoint != null) {
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

      if (savedUserCoupon != null) {
        savedUserCoupon.rollbackUsage();
        try {
          userCouponRepository.save(savedUserCoupon);
        } catch (Exception rollbackException) {
          // TODO: 로깅 추가
        }
      }

      if (savedPointTransaction != null) {
        try {
          pointTransactionRepository.deleteById(savedPointTransaction.getId());
        } catch (Exception rollbackException) {
          // TODO: 로깅 추가
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
}
