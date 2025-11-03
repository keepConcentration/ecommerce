package com.phm.ecommerce.application.usecase.order;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyUsedException;
import com.phm.ecommerce.domain.coupon.exception.CouponExpiredException;
import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.domain.point.exception.InsufficientPointsException;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.InsufficientStockException;
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

  public record Input(
      Long userId,
      Long productId,
      Long quantity,
      Long userCouponId) {}

  // TODO 기능 분리
  public Output execute(Input request) {
    // 1. Product 검증 및 재고 확인
    Product product = productRepository.findByIdOrThrow(request.productId());

    if (!product.hasEnoughStock(request.quantity())) {
      throw new InsufficientStockException();
    }

    // catch 블록에서 접근 가능하도록 변수 선언
    UserCoupon userCoupon = null;
    Long discountAmount = 0L;
    Long totalAmount = 0L;
    Long finalAmount = 0L;

    try {
      // 2. 재고 즉시 차감
      product.decreaseStock(request.quantity());
      productRepository.save(product);

      // 3. 쿠폰 검증 및 할인 금액 계산
      if (request.userCouponId() != null) {
        userCoupon = userCouponRepository.findByIdOrThrow(request.userCouponId());

        if (userCoupon.isUsed()) {
          throw new CouponAlreadyUsedException();
        }

        if (userCoupon.isExpired()) {
          throw new CouponExpiredException();
        }

        Coupon coupon = couponRepository.findByIdOrThrow(userCoupon.getCouponId());

        discountAmount = coupon.getDiscountAmount();
      }

      // 4. 금액 계산
      totalAmount = product.getPrice() * request.quantity();
      finalAmount = totalAmount - discountAmount;

      // 5. Point 검증 및 차감
      Point point = pointRepository.findByUserIdOrThrow(request.userId());

      if (!point.hasEnough(finalAmount)) {
        throw new InsufficientPointsException();
      }

      point.deduct(finalAmount);
      pointRepository.save(point);

      // 6. Order 생성
      Order order = Order.create(request.userId(), totalAmount, discountAmount);
      order = orderRepository.save(order);

      // 7. OrderItem 생성
      OrderItem orderItem = OrderItem.create(
          order.getId(),
          request.userId(),
          request.productId(),
          product.getName(),
          request.quantity(),
          product.getPrice(),
          discountAmount,
          request.userCouponId()
      );
      orderItem = orderItemRepository.save(orderItem);

      // 8. 쿠폰 사용 처리
      if (userCoupon != null) {
        userCoupon.use();
        userCouponRepository.save(userCoupon);
      }

      // 9. PointTransaction 생성
      PointTransaction pointTransaction = PointTransaction.createDeduction(
          point.getId(),
          order.getId(),
          finalAmount
      );
      pointTransactionRepository.save(pointTransaction);

      // 10. Response 반환
      OrderItemInfo orderItemInfo = new OrderItemInfo(
          orderItem.getId(),
          orderItem.getProductId(),
          orderItem.getProductName(),
          orderItem.getQuantity(),
          orderItem.getPrice(),
          orderItem.getTotalPrice(),
          orderItem.getDiscountAmount(),
          orderItem.getFinalAmount(),
          orderItem.getUserCouponId()
      );

      return new Output(
          order.getId(),
          order.getUserId(),
          order.getTotalAmount(),
          order.getDiscountAmount(),
          order.getFinalAmount(),
          order.getCreatedAt(),
          List.of(orderItemInfo)
      );

    } catch (Exception e) {
      // 예외 발생 시 모든 변경사항 롤백

      // 재고 롤백
      product.increaseStock(request.quantity());
      productRepository.save(product);

      // 포인트 롤백 (포인트 차감 이후 예외 발생한 경우)
      try {
        Point point = pointRepository.findByUserId(request.userId()).orElse(null);
        if (point != null && finalAmount > 0) {
          point.charge(finalAmount);
          pointRepository.save(point);
        }
      } catch (Exception rollbackException) {
        // 포인트 롤백 실패 시 로그만 남기고 원래 예외 throw
        // TODO: 로깅 추가
      }

      // 쿠폰 롤백 (쿠폰 사용 처리 이후 예외 발생한 경우)
      if (userCoupon != null && userCoupon.isUsed()) {
        try {
          userCoupon.rollbackUsage();
          userCouponRepository.save(userCoupon);
        } catch (Exception rollbackException) {
          // 쿠폰 롤백 실패 시 로그만 남기고 계속 진행
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
