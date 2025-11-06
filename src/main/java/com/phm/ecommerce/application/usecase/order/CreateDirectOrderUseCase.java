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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
    log.info("즉시 구매 주문 생성 시작 - userId: {}, productId: {}, quantity: {}",
        request.userId(), request.productId(), request.quantity());

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

      log.debug("재고 차감 완료 - productId: {}, quantity: {}, remainingStock: {}",
          request.productId(), request.quantity(), savedProduct.getQuantity());

      UserCoupon userCoupon = null;
      Long discountAmount = 0L;
      if (request.userCouponId() != null) {
        userCoupon = userCouponRepository.findByIdOrThrow(request.userCouponId());
        Coupon coupon = couponRepository.findByIdOrThrow(userCoupon.getCouponId());
        discountAmount = userCoupon.calculateDiscount(coupon);
      }

      Long totalAmount = orderPricingService.calculateItemTotal(savedProduct, request.quantity());
      finalAmount = orderPricingService.calculateFinalAmount(totalAmount, discountAmount);

      log.debug("주문 금액 계산 완료 - totalAmount: {}, discountAmount: {}, finalAmount: {}",
          totalAmount, discountAmount, finalAmount);

      Point point = pointRepository.findByUserIdOrThrow(request.userId());
      point.deduct(finalAmount);
      savedPoint = pointRepository.save(point);

      log.debug("포인트 차감 완료 - userId: {}, deductedAmount: {}, remainingPoints: {}",
          request.userId(), finalAmount, savedPoint.getAmount());

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

      log.info("즉시 구매 주문 생성 완료 - orderId: {}, userId: {}, finalAmount: {}",
          savedOrder.getId(), savedOrder.getUserId(), savedOrder.getFinalAmount());

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
      log.warn("즉시 구매 주문 생성 중 오류 발생, 롤백 시작 - userId: {}, error: {}",
          request.userId(), e.getMessage());
      if (savedProduct != null) {
        savedProduct.increaseStock(request.quantity());
        try {
          productRepository.save(savedProduct);
          log.debug("재고 롤백 완료 - productId: {}, quantity: {}",
              savedProduct.getId(), request.quantity());
        } catch (Exception rollbackException) {
          log.error("재고 롤백 실패 - productId: {}, quantity: {}, error: {}",
              savedProduct.getId(), request.quantity(), rollbackException.getMessage());
        }
      }

      if (savedPoint != null) {
        savedPoint.charge(finalAmount);
        try {
          pointRepository.save(savedPoint);
          log.debug("포인트 롤백 완료 - userId: {}, chargedAmount: {}",
              request.userId(), finalAmount);
        } catch (Exception rollbackException) {
          log.error("포인트 롤백 실패 - userId: {}, chargedAmount: {}, error: {}",
              request.userId(), finalAmount, rollbackException.getMessage());
        }
      }

      if (savedOrder != null) {
        try {
          orderItemRepository.deleteByOrderId(savedOrder.getId());
          orderRepository.deleteById(savedOrder.getId());
          log.debug("주문 데이터 롤백 완료 - orderId: {}", savedOrder.getId());
        } catch (Exception rollbackException) {
          log.error("주문 데이터 롤백 실패 - orderId: {}, error: {}",
              savedOrder.getId(), rollbackException.getMessage());
        }
      }

      if (savedUserCoupon != null) {
        savedUserCoupon.rollbackUsage();
        try {
          userCouponRepository.save(savedUserCoupon);
          log.debug("쿠폰 사용 롤백 완료 - userCouponId: {}", savedUserCoupon.getId());
        } catch (Exception rollbackException) {
          log.error("쿠폰 사용 롤백 실패 - userCouponId: {}, error: {}",
              savedUserCoupon.getId(), rollbackException.getMessage());
        }
      }

      if (savedPointTransaction != null) {
        try {
          pointTransactionRepository.deleteById(savedPointTransaction.getId());
          log.debug("포인트 거래 내역 롤백 완료 - transactionId: {}", savedPointTransaction.getId());
        } catch (Exception rollbackException) {
          log.error("포인트 거래 내역 롤백 실패 - transactionId: {}, error: {}",
              savedPointTransaction.getId(), rollbackException.getMessage());
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
