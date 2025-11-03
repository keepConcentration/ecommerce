package com.phm.ecommerce.application.usecase.order;

import com.phm.ecommerce.domain.cart.CartItem;
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
import com.phm.ecommerce.persistence.repository.CartItemRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  public record Input(
      Long userId,
      List<CartItemCouponInfo> cartItemCouponMaps) {}

  public record CartItemCouponInfo(
      Long cartItemId,
      Long userCouponId) {}

  public Output execute(Input request) {
    // 1. 주문할 장바구니 아이템 조회
    List<CartItem> cartItems = new ArrayList<>();
    for (CartItemCouponInfo map : request.cartItemCouponMaps()) {
      CartItem cartItem = cartItemRepository.findByIdOrThrow(map.cartItemId());

      // 본인의 장바구니 아이템인지 확인
      if (!cartItem.getUserId().equals(request.userId())) {
        throw new IllegalArgumentException("본인의 장바구니 아이템이 아닙니다: " + map.cartItemId());
      }
      cartItems.add(cartItem);
    }

    if (cartItems.isEmpty()) {
      throw new IllegalStateException("주문할 장바구니 아이템이 없습니다");
    }

    // 2. CartItemCouponInfo을 Map으로 변환 (빠른 조회)
    Map<Long, Long> cartItemCouponMap = new HashMap<>();
    if (request.cartItemCouponMaps() != null) {
      for (CartItemCouponInfo map : request.cartItemCouponMaps()) {
        cartItemCouponMap.put(map.cartItemId(), map.userCouponId());
      }
    }

    // 3. 각 CartItem에 대해 상품 조회, 재고 확인 및 차감
    List<OrderItemData> orderItemDataList = new ArrayList<>();
    Long totalAmount = 0L;
    Long totalDiscountAmount = 0L;

    try {
      for (CartItem cartItem : cartItems) {
        // 상품 조회
        Product product = productRepository.findByIdOrThrow(cartItem.getProductId());

        // 재고 확인 및 즉시 차감
        if (!product.hasEnoughStock(cartItem.getQuantity())) {
          throw new InsufficientStockException();
        }
        product.decreaseStock(cartItem.getQuantity());
        productRepository.save(product);

        // 쿠폰 처리
        Long discountAmount = 0L;
        UserCoupon userCoupon = null;
        Long userCouponId = cartItemCouponMap.get(cartItem.getId());

        if (userCouponId != null) {
          userCoupon = userCouponRepository.findByIdOrThrow(userCouponId);

          if (userCoupon.isUsed()) {
            throw new CouponAlreadyUsedException();
          }

          if (userCoupon.isExpired()) {
            throw new CouponExpiredException();
          }

          Coupon coupon = couponRepository.findByIdOrThrow(userCoupon.getCouponId());

          discountAmount = coupon.getDiscountAmount();
        }

        // OrderItemData 저장
        orderItemDataList.add(new OrderItemData(cartItem, product, userCoupon, discountAmount));

        // 금액 계산
        Long itemTotalAmount = product.getPrice() * cartItem.getQuantity();
        totalAmount += itemTotalAmount;
        totalDiscountAmount += discountAmount;
      }

      // 4. 최종 금액 계산
      Long finalAmount = totalAmount - totalDiscountAmount;

      // 5. Point 검증 및 차감
      Point point = pointRepository.findByUserIdOrThrow(request.userId());

      if (!point.hasEnough(finalAmount)) {
        throw new InsufficientPointsException();
      }

      point.deduct(finalAmount);
      pointRepository.save(point);

      // 6. Order 생성
      Order order = Order.create(request.userId(), totalAmount, totalDiscountAmount);
      order = orderRepository.save(order);

      // 7. OrderItem 생성 및 쿠폰 사용 처리
      List<OrderItemInfo> orderItemInfos = new ArrayList<>();

      for (OrderItemData data : orderItemDataList) {
        OrderItem orderItem =
            OrderItem.create(
                order.getId(),
                request.userId(),
                data.product.getId(),
                data.product.getName(),
                data.cartItem.getQuantity(),
                data.product.getPrice(),
                data.discountAmount,
                data.userCoupon != null ? data.userCoupon.getId() : null);
        orderItem = orderItemRepository.save(orderItem);

        // 쿠폰 사용 처리
        if (data.userCoupon != null) {
          data.userCoupon.use();
          userCouponRepository.save(data.userCoupon);
        }

        // Response 생성
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

      // 8. PointTransaction 생성
      PointTransaction pointTransaction =
          PointTransaction.createDeduction(point.getId(), order.getId(), finalAmount);
      pointTransactionRepository.save(pointTransaction);

      // 9. 주문한 장바구니 아이템만 삭제
      for (CartItem cartItem : cartItems) {
        cartItemRepository.deleteById(cartItem.getId());
      }

      // 10. Response 반환
      return new Output(
          order.getId(),
          order.getUserId(),
          order.getTotalAmount(),
          order.getDiscountAmount(),
          order.getFinalAmount(),
          order.getCreatedAt(),
          orderItemInfos);

    } catch (Exception e) {
      // 예외 발생 시 모든 변경사항 롤백

      // 재고 롤백
      for (OrderItemData data : orderItemDataList) {
        data.product.increaseStock(data.cartItem.getQuantity());
        productRepository.save(data.product);
      }

      // 포인트 롤백 (포인트 차감 이후 예외 발생한 경우)
      try {
        Point point = pointRepository.findByUserId(request.userId()).orElse(null);
        if (point != null && totalDiscountAmount != null) {
          Long finalAmount = totalAmount - totalDiscountAmount;
          point.charge(finalAmount);
          pointRepository.save(point);
        }
      } catch (Exception rollbackException) {
        // 포인트 롤백 실패 시 로그만 남기고 원래 예외 throw
        // TODO: 로깅 추가
      }

      // 쿠폰 롤백 (쿠폰 사용 처리 이후 예외 발생한 경우)
      for (OrderItemData data : orderItemDataList) {
        if (data.userCoupon != null && data.userCoupon.isUsed()) {
          try {
            data.userCoupon.rollbackUsage();
            userCouponRepository.save(data.userCoupon);
          } catch (Exception rollbackException) {
            // 쿠폰 롤백 실패 시 로그만 남기고 계속 진행
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
