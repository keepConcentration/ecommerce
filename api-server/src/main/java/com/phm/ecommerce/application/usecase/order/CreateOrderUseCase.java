package com.phm.ecommerce.application.usecase.order;

import com.phm.ecommerce.application.lock.MultiDistributedLock;
import com.phm.ecommerce.application.service.ProductService;
import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.domain.order.OrderPricingService;
import com.phm.ecommerce.domain.order.exception.EmptyCartException;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.infrastructure.repository.CartItemRepository;
import com.phm.ecommerce.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.infrastructure.repository.OrderItemRepository;
import com.phm.ecommerce.infrastructure.repository.OrderRepository;
import com.phm.ecommerce.infrastructure.repository.PointRepository;
import com.phm.ecommerce.infrastructure.repository.PointTransactionRepository;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import com.phm.ecommerce.infrastructure.repository.UserCouponRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final ProductService productService;
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

  @MultiDistributedLock(lockKeyProvider = "prepareLockKeys")
  @Transactional
  public Output execute(Input request) {
    log.info("주문 생성 시작 - userId: {}, cartItemCount: {}",
        request.userId(), request.cartItemCouponMaps().size());

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

    List<OrderItemData> orderItemDataList = new ArrayList<>();
    Long totalAmount = 0L;
    Long totalDiscountAmount = 0L;

    for (CartItem cartItem : cartItems) {
      Product product = productRepository.findByIdOrThrow(cartItem.getProductId());
      product.decreaseStock(cartItem.getQuantity());
      product.increaseSalesCount(cartItem.getQuantity());
      product = productService.saveProduct(product);

      Long discountAmount = 0L;
      UserCoupon userCoupon = null;
      Long userCouponId = cartItemCouponMap.get(cartItem.getId());

      if (userCouponId != null) {
        userCoupon = userCouponRepository.findByIdOrThrow(userCouponId);
        Coupon coupon = couponRepository.findByIdOrThrow(userCoupon.getCouponId());
        discountAmount = userCoupon.calculateDiscount(coupon);
      }

      orderItemDataList.add(new OrderItemData(cartItem, product, userCoupon, discountAmount));

      Long itemTotalAmount = orderPricingService.calculateItemTotal(product, cartItem.getQuantity());
      totalAmount += itemTotalAmount;
      totalDiscountAmount += discountAmount;
    }

    Long finalAmount = orderPricingService.calculateFinalAmount(totalAmount, totalDiscountAmount);

    log.debug("주문 금액 계산 완료 - totalAmount: {}, discountAmount: {}, finalAmount: {}",
        totalAmount, totalDiscountAmount, finalAmount);

    Point point = pointRepository.findByUserIdOrThrow(request.userId());
    point.deduct(finalAmount);
    point = pointRepository.save(point);

    log.debug("포인트 차감 완료 - userId: {}, deductedAmount: {}, remainingPoints: {}",
        request.userId(), finalAmount, point.getAmount());

    Order order = Order.create(request.userId(), totalAmount, totalDiscountAmount);
    order = orderRepository.save(order);

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

      if (data.userCoupon != null) {
        data.userCoupon.use();
        userCouponRepository.save(data.userCoupon);
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
        PointTransaction.createDeduction(point.getId(), order.getId(), finalAmount);
    pointTransactionRepository.save(pointTransaction);

    for (CartItem cartItem : cartItems) {
      cartItemRepository.deleteById(cartItem.getId());
    }

    log.info("주문 생성 완료 - orderId: {}, userId: {}, finalAmount: {}, orderItemCount: {}",
        order.getId(), order.getUserId(), order.getFinalAmount(), orderItemInfos.size());

    return new Output(
        order.getId(),
        order.getUserId(),
        order.getTotalAmount(),
        order.getDiscountAmount(),
        order.getFinalAmount(),
        order.getCreatedAt(),
        orderItemInfos);
  }

  private List<String> prepareLockKeys(Input request) {
    List<String> lockKeys = new ArrayList<>();

    List<CartItem> cartItems = new ArrayList<>();
    for (CartItemCouponInfo map : request.cartItemCouponMaps()) {
      CartItem cartItem = cartItemRepository.findByIdOrThrow(map.cartItemId());
      cartItem.validateOwnership(request.userId());
      cartItems.add(cartItem);
    }

    cartItems.stream()
        .map(CartItem::getProductId)
        .distinct()
        .forEach(productId -> lockKeys.add("product:" + productId));

    lockKeys.add("point:user:" + request.userId());

    log.debug("락 키 준비 완료 - lockKeys: {}", lockKeys);
    return lockKeys;
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

    private record OrderItemData(CartItem cartItem, Product product, UserCoupon userCoupon, Long discountAmount) {

  }
}
