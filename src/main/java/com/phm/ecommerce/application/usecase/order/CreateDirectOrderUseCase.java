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
import com.phm.ecommerce.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.presentation.dto.response.OrderItemResponse;
import com.phm.ecommerce.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

  // TODO 기능 분리
  public OrderResponse execute(DirectOrderRequest request) {
    Product product = productRepository.findById(request.productId())
        .orElseThrow(ProductNotFoundException::new);

    if (!product.hasEnoughStock(request.quantity())) {
      throw new InsufficientStockException();
    }

    Long discountAmount = 0L;
    UserCoupon userCoupon = null;

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

    Long totalAmount = product.getPrice() * request.quantity();
    Long finalAmount = totalAmount - discountAmount;

    Point point = pointRepository.findByUserId(request.userId())
        .orElseThrow(InsufficientPointsException::new);

    if (!point.hasEnough(finalAmount)) {
      throw new InsufficientPointsException();
    }

    point.deduct(finalAmount);
    pointRepository.save(point);

    product.decreaseStock(request.quantity());
    productRepository.save(product);

    Order order = Order.create(request.userId(), totalAmount, discountAmount);
    order = orderRepository.save(order);

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

    if (userCoupon != null) {
      userCoupon.use();
      userCouponRepository.save(userCoupon);
    }

    PointTransaction pointTransaction = PointTransaction.createDeduction(
        point.getId(),
        order.getId(),
        finalAmount
    );
    pointTransactionRepository.save(pointTransaction);

    OrderItemResponse orderItemResponse = new OrderItemResponse(
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

    return new OrderResponse(
        order.getId(),
        order.getUserId(),
        order.getTotalAmount(),
        order.getDiscountAmount(),
        order.getFinalAmount(),
        order.getCreatedAt(),
        List.of(orderItemResponse)
    );
  }
}
