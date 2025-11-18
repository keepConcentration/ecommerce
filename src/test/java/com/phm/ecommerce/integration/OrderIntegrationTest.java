package com.phm.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponErrorCode;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.exception.PointErrorCode;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductErrorCode;
import com.phm.ecommerce.infrastructure.repository.*;
import com.phm.ecommerce.presentation.dto.request.CartItemCouponMap;
import com.phm.ecommerce.presentation.dto.request.CreateOrderRequest;
import com.phm.ecommerce.presentation.dto.request.DirectOrderRequest;
import com.phm.ecommerce.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("주문 통합 테스트 (Controller + UseCase)")
class OrderIntegrationTest extends TestContainerSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CartItemRepository cartItemRepository;

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  private UserCouponRepository userCouponRepository;

  @Autowired
  private PointRepository pointRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  private Long testUserId = 1L;
  private Product testProduct;
  private Point testPoint;

  @BeforeEach
  void setUp() {
    // 테스트용 상품 생성
    testProduct = Product.create("테스트 상품", 10000L, 100L);
    testProduct = productRepository.save(testProduct);

    // 테스트용 포인트 생성 및 충전
    testPoint = Point.create(testUserId);
    testPoint.charge(100000L);
    testPoint = pointRepository.save(testPoint);
  }

  @Test
  @DisplayName("장바구니 기반 주문 생성 - 성공 (쿠폰 미사용)")
  void createOrder_Success_NoCoupon() throws Exception {
    // given
    CartItem cartItem = CartItem.create(testUserId, testProduct.getId(), 2L);
    cartItem = cartItemRepository.save(cartItem);

    CartItemCouponMap cartItemCouponMap = new CartItemCouponMap(cartItem.getId(), null);
    CreateOrderRequest request = new CreateOrderRequest(testUserId, List.of(cartItemCouponMap));

    // when & then
    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.userId").value(testUserId))
        .andExpect(jsonPath("$.data.totalAmount").value(20000)) // 10000 * 2
        .andExpect(jsonPath("$.data.discountAmount").value(0))
        .andExpect(jsonPath("$.data.finalAmount").value(20000))
        .andExpect(jsonPath("$.data.orderItems").isArray())
        .andExpect(jsonPath("$.data.orderItems.length()").value(1));

    // 장바구니에서 삭제되었는지 확인
    assertThat(cartItemRepository.findById(cartItem.getId())).isEmpty();

    // 재고가 차감되었는지 확인
    Product updatedProduct = productRepository.findByIdOrThrow(testProduct.getId());
    assertThat(updatedProduct.getQuantity()).isEqualTo(98L); // 100 - 2
  }

  @Test
  @DisplayName("장바구니 기반 주문 생성 - 성공 (쿠폰 사용)")
  void createOrder_Success_WithCoupon() throws Exception {
    // given
    CartItem cartItem = CartItem.create(testUserId, testProduct.getId(), 3L);
    cartItem = cartItemRepository.save(cartItem);

    Coupon coupon = Coupon.create("할인 쿠폰", 3000L, 10L, 30);
    coupon = couponRepository.save(coupon);
    UserCoupon userCoupon = UserCoupon.issue(testUserId, coupon.getId(), 30);
    userCoupon = userCouponRepository.save(userCoupon);

    CartItemCouponMap cartItemCouponMap = new CartItemCouponMap(cartItem.getId(), userCoupon.getId());
    CreateOrderRequest request = new CreateOrderRequest(testUserId, List.of(cartItemCouponMap));

    // when & then
    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.userId").value(testUserId))
        .andExpect(jsonPath("$.data.totalAmount").value(30000)) // 10000 * 3
        .andExpect(jsonPath("$.data.discountAmount").value(3000))
        .andExpect(jsonPath("$.data.finalAmount").value(27000)); // 30000 - 3000

    // 쿠폰이 사용 처리되었는지 확인
    UserCoupon usedCoupon = userCouponRepository.findByIdOrThrow(userCoupon.getId());
    assertThat(usedCoupon.isUsed()).isTrue();
  }

  @Test
  @DisplayName("장바구니 기반 주문 생성 - 실패 (재고 부족)")
  void createOrder_InsufficientStock() throws Exception {
    // given
    CartItem cartItem = CartItem.create(testUserId, testProduct.getId(), 200L); // 재고보다 많은 수량
    cartItem = cartItemRepository.save(cartItem);

    CartItemCouponMap cartItemCouponMap = new CartItemCouponMap(cartItem.getId(), null);
    CreateOrderRequest request = new CreateOrderRequest(testUserId, List.of(cartItemCouponMap));

    // when & then
    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(ProductErrorCode.INSUFFICIENT_STOCK.getCode()));
  }

  @Test
  @DisplayName("장바구니 기반 주문 생성 - 실패 (포인트 부족)")
  void createOrder_InsufficientPoints() throws Exception {
    // given - 포인트가 부족한 사용자
    Long poorUserId = 2L;
    Point poorPoint = Point.create(poorUserId);
    poorPoint.charge(1000L); // 부족한 포인트
    pointRepository.save(poorPoint);

    CartItem cartItem = CartItem.create(poorUserId, testProduct.getId(), 5L);
    cartItem = cartItemRepository.save(cartItem);

    CartItemCouponMap cartItemCouponMap = new CartItemCouponMap(cartItem.getId(), null);
    CreateOrderRequest request = new CreateOrderRequest(poorUserId, List.of(cartItemCouponMap));

    // when & then
    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(PointErrorCode.INSUFFICIENT_POINTS.getCode()));
  }

  @Test
  @DisplayName("장바구니 기반 주문 생성 - 실패 (만료된 쿠폰)")
  void createOrder_ExpiredCoupon() throws Exception {
    // given
    CartItem cartItem = CartItem.create(testUserId, testProduct.getId(), 2L);
    cartItem = cartItemRepository.save(cartItem);

    Coupon coupon = Coupon.create("할인 쿠폰", 3000L, 10L, 30);
    coupon = couponRepository.save(coupon);
    UserCoupon userCoupon = UserCoupon.issue(testUserId, coupon.getId(), -1); // 만료된 쿠폰
    userCoupon = userCouponRepository.save(userCoupon);

    CartItemCouponMap cartItemCouponMap = new CartItemCouponMap(cartItem.getId(), userCoupon.getId());
    CreateOrderRequest request = new CreateOrderRequest(testUserId, List.of(cartItemCouponMap));

    // when & then
    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(CouponErrorCode.COUPON_EXPIRED.getCode()));
  }

  @Test
  @DisplayName("상품 바로 주문 - 성공 (쿠폰 미사용)")
  void createDirectOrder_Success_NoCoupon() throws Exception {
    // given
    DirectOrderRequest request = new DirectOrderRequest(
        testUserId,
        testProduct.getId(),
        3L,
        null
    );

    // when & then
    mockMvc.perform(post("/api/v1/orders/direct")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.userId").value(testUserId))
        .andExpect(jsonPath("$.data.totalAmount").value(30000)) // 10000 * 3
        .andExpect(jsonPath("$.data.discountAmount").value(0))
        .andExpect(jsonPath("$.data.finalAmount").value(30000))
        .andExpect(jsonPath("$.data.orderItems.length()").value(1));

    // 재고가 차감되었는지 확인
    Product updatedProduct = productRepository.findByIdOrThrow(testProduct.getId());
    assertThat(updatedProduct.getQuantity()).isEqualTo(97L); // 100 - 3
  }

  @Test
  @DisplayName("상품 바로 주문 - 성공 (쿠폰 사용)")
  void createDirectOrder_Success_WithCoupon() throws Exception {
    // given
    Coupon coupon = Coupon.create("할인 쿠폰", 5000L, 10L, 30);
    coupon = couponRepository.save(coupon);
    UserCoupon userCoupon = UserCoupon.issue(testUserId, coupon.getId(), 30);
    userCoupon = userCouponRepository.save(userCoupon);

    DirectOrderRequest request = new DirectOrderRequest(
        testUserId,
        testProduct.getId(),
        2L,
        userCoupon.getId()
    );

    // when & then
    mockMvc.perform(post("/api/v1/orders/direct")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.totalAmount").value(20000)) // 10000 * 2
        .andExpect(jsonPath("$.data.discountAmount").value(5000))
        .andExpect(jsonPath("$.data.finalAmount").value(15000)); // 20000 - 5000
  }

  @Test
  @DisplayName("상품 바로 주문 - 실패 (존재하지 않는 상품)")
  void createDirectOrder_ProductNotFound() throws Exception {
    // given
    DirectOrderRequest request = new DirectOrderRequest(
        testUserId,
        999L,
        1L,
        null
    );

    // when & then
    mockMvc.perform(post("/api/v1/orders/direct")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(ProductErrorCode.PRODUCT_NOT_FOUND.getCode()));
  }

  @Test
  @DisplayName("상품 바로 주문 - 실패 (재고 부족)")
  void createDirectOrder_InsufficientStock() throws Exception {
    // given
    DirectOrderRequest request = new DirectOrderRequest(
        testUserId,
        testProduct.getId(),
        200L, // 재고보다 많은 수량
        null
    );

    // when & then
    mockMvc.perform(post("/api/v1/orders/direct")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(ProductErrorCode.INSUFFICIENT_STOCK.getCode()));
  }
}
