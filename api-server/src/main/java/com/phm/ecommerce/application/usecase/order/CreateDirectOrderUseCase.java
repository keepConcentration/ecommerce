package com.phm.ecommerce.application.usecase.order;

import com.phm.ecommerce.application.lock.MultiDistributedLock;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.domain.order.OrderPricingService;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.infrastructure.repository.OrderItemRepository;
import com.phm.ecommerce.infrastructure.repository.OrderRepository;
import com.phm.ecommerce.infrastructure.repository.PointRepository;
import com.phm.ecommerce.infrastructure.repository.PointTransactionRepository;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import com.phm.ecommerce.infrastructure.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @MultiDistributedLock(lockKeyProvider = "prepareLockKeys")
  @Transactional
  public Output execute(Input request) {
    log.info("즉시 구매 주문 생성 시작 - userId: {}, productId: {}, quantity: {}",
        request.userId(), request.productId(), request.quantity());

    Product product = productRepository.findByIdOrThrow(request.productId());
    product.decreaseStock(request.quantity());
    product.increaseSalesCount(request.quantity());
    product = productRepository.save(product);

    log.debug("재고 차감 완료 - productId: {}, quantity: {}, remainingStock: {}",
        request.productId(), request.quantity(), product.getQuantity());

    UserCoupon userCoupon = null;
    Long discountAmount = 0L;
    if (request.userCouponId() != null) {
      userCoupon = userCouponRepository.findByIdOrThrow(request.userCouponId());
      Coupon coupon = couponRepository.findByIdOrThrow(userCoupon.getCouponId());
      discountAmount = userCoupon.calculateDiscount(coupon);
    }

    Long totalAmount = orderPricingService.calculateItemTotal(product, request.quantity());
    Long finalAmount = orderPricingService.calculateFinalAmount(totalAmount, discountAmount);

    log.debug("주문 금액 계산 완료 - totalAmount: {}, discountAmount: {}, finalAmount: {}",
        totalAmount, discountAmount, finalAmount);

    Point point = pointRepository.findByUserIdOrThrow(request.userId());
    point.deduct(finalAmount);
    point = pointRepository.save(point);

    log.debug("포인트 차감 완료 - userId: {}, deductedAmount: {}, remainingPoints: {}",
        request.userId(), finalAmount, point.getAmount());

    Order order = Order.create(request.userId(), totalAmount, discountAmount);
    order = orderRepository.save(order);

    OrderItem orderItem =
        OrderItem.create(
            order.getId(),
            request.userId(),
            request.productId(),
            product.getName(),
            request.quantity(),
            product.getPrice(),
            discountAmount,
            request.userCouponId());
    orderItem = orderItemRepository.save(orderItem);

    if (userCoupon != null) {
      userCoupon.use();
      userCouponRepository.save(userCoupon);
    }

    PointTransaction pointTransaction =
        PointTransaction.createDeduction(point.getId(), order.getId(), finalAmount);
    pointTransactionRepository.save(pointTransaction);

    log.info("즉시 구매 주문 생성 완료 - orderId: {}, userId: {}, finalAmount: {}",
        order.getId(), order.getUserId(), order.getFinalAmount());

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
        order.getId(),
        order.getUserId(),
        order.getTotalAmount(),
        order.getDiscountAmount(),
        order.getFinalAmount(),
        order.getCreatedAt(),
        List.of(orderItemInfo));
  }

  private List<String> prepareLockKeys(Input request) {
    return List.of(
        "product:" + request.productId(),
        "point:user:" + request.userId()
    );
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
