package com.phm.ecommerce.application.usecase.order;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponAlreadyUsedException;
import com.phm.ecommerce.domain.coupon.exception.CouponExpiredException;
import com.phm.ecommerce.domain.coupon.exception.CouponNotFoundException;
import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.domain.point.exception.InsufficientPointsException;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.InsufficientStockException;
import com.phm.ecommerce.domain.product.exception.ProductNotFoundException;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.OrderItemRepository;
import com.phm.ecommerce.persistence.repository.OrderRepository;
import com.phm.ecommerce.persistence.repository.PointRepository;
import com.phm.ecommerce.persistence.repository.PointTransactionRepository;
import com.phm.ecommerce.persistence.repository.ProductRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

  @Schema(description = "직접 주문 생성 요청")
  public record Input(
      @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotNull(message = "사용자 ID는 필수입니다")
      Long userId,

      @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotNull(message = "상품 ID는 필수입니다")
      Long productId,

      @Schema(description = "주문 수량", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotNull(message = "수량은 필수입니다")
      @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
      Long quantity,

      @Schema(description = "사용할 쿠폰 ID (선택)", example = "10")
      Long userCouponId) {}

  // TODO 기능 분리
  public Output execute(Input request) {
    // 1. Product 검증 및 재고 확인
    Product product = productRepository.findById(request.productId())
        .orElseThrow(ProductNotFoundException::new);

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
        userCoupon = userCouponRepository.findById(request.userCouponId())
            .orElseThrow(CouponNotFoundException::new);

        if (userCoupon.isUsed()) {
          throw new CouponAlreadyUsedException();
        }

        if (userCoupon.isExpired()) {
          throw new CouponExpiredException();
        }

        Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
            .orElseThrow(CouponNotFoundException::new);

        discountAmount = coupon.getDiscountAmount();
      }

      // 4. 금액 계산
      totalAmount = product.getPrice() * request.quantity();
      finalAmount = totalAmount - discountAmount;

      // 5. Point 검증 및 차감
      Point point = pointRepository.findByUserId(request.userId())
          .orElseThrow(InsufficientPointsException::new);

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

  @Schema(description = "주문 정보")
  public record Output(
      @Schema(description = "주문 ID", example = "1")
      Long orderId,

      @Schema(description = "사용자 ID", example = "1")
      Long userId,

      @Schema(description = "전체 주문 금액", example = "3035000")
      Long totalAmount,

      @Schema(description = "전체 할인 금액", example = "55000")
      Long discountAmount,

      @Schema(description = "최종 결제 금액", example = "2980000")
      Long finalAmount,

      @Schema(description = "주문 생성일시", example = "2025-01-20T15:30:00")
      LocalDateTime createdAt,

      @Schema(description = "주문 아이템 목록")
      List<OrderItemInfo> orderItems) {}

  @Schema(description = "주문 아이템 정보")
  public record OrderItemInfo(
      @Schema(description = "주문 아이템 ID", example = "1")
      Long orderItemId,

      @Schema(description = "상품 ID", example = "1")
      Long productId,

      @Schema(description = "상품명", example = "노트북")
      String productName,

      @Schema(description = "수량", example = "2")
      Long quantity,

      @Schema(description = "단가", example = "1500000")
      Long price,

      @Schema(description = "총 가격", example = "3000000")
      Long totalPrice,

      @Schema(description = "할인 금액", example = "50000")
      Long discountAmount,

      @Schema(description = "최종 금액", example = "2950000")
      Long finalAmount,

      @Schema(description = "사용한 쿠폰 ID", example = "10")
      Long userCouponId) {}
}
