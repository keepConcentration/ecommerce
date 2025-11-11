package com.phm.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.user.User;
import com.phm.ecommerce.infrastructure.repository.CartItemRepository;
import com.phm.ecommerce.infrastructure.repository.PointRepository;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import com.phm.ecommerce.infrastructure.repository.UserRepository;
import com.phm.ecommerce.presentation.dto.request.CartItemCouponMap;
import com.phm.ecommerce.presentation.dto.request.CreateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PopularProductIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PointRepository pointRepository;

  @Autowired
  private CartItemRepository cartItemRepository;

  private User user;
  private Product product1;
  private Product product2;
  private Product product3;

  @BeforeEach
  void setUp() {
    // 사용자 생성
    user = User.create();
    user = userRepository.save(user);

    // 포인트 생성 및 충전
    Point point = Point.create(user.getId());
    point.charge(1000000000L); // 10억 포인트
    pointRepository.save(point);

    // 상품 3개 생성
    product1 = Product.create("노트북", 1500000L, 100L);
    product1 = productRepository.save(product1);

    product2 = Product.create("키보드", 120000L, 100L);
    product2 = productRepository.save(product2);

    product3 = Product.create("마우스", 50000L, 100L);
    product3 = productRepository.save(product3);
  }

  @Test
  @DisplayName("인기 상품 조회 - 기본 가중치 (조회수 0.1, 판매량 0.9)")
  void getPopularProducts_withDefaultWeights() throws Exception {
    // given
    // product1: viewCount=100, salesCount=50 -> score = 100*0.1 + 50*0.9 = 55
    // product2: viewCount=200, salesCount=30 -> score = 200*0.1 + 30*0.9 = 47
    // product3: viewCount=50, salesCount=100 -> score = 50*0.1 + 100*0.9 = 95
    for (int i = 0; i < 100; i++) {
      product1.increaseViewCount();
    }
    productRepository.save(product1);

    for (int i = 0; i < 200; i++) {
      product2.increaseViewCount();
    }
    productRepository.save(product2);

    for (int i = 0; i < 50; i++) {
      product3.increaseViewCount();
    }
    productRepository.save(product3);

    createOrder(user.getId(), product1.getId(), 50L);
    createOrder(user.getId(), product2.getId(), 30L);
    createOrder(user.getId(), product3.getId(), 100L);

    // when & then
    mockMvc
        .perform(get("/api/v1/products/popular"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].productId").value(product3.getId()))
        .andExpect(jsonPath("$.data[0].viewCount").value(50))
        .andExpect(jsonPath("$.data[0].salesCount").value(100))
        .andExpect(jsonPath("$.data[0].popularityScore").value(95.0))
        .andExpect(jsonPath("$.data[1].productId").value(product1.getId()))
        .andExpect(jsonPath("$.data[1].viewCount").value(100))
        .andExpect(jsonPath("$.data[1].salesCount").value(50))
        .andExpect(jsonPath("$.data[1].popularityScore").value(55.0))
        .andExpect(jsonPath("$.data[2].productId").value(product2.getId()))
        .andExpect(jsonPath("$.data[2].viewCount").value(200))
        .andExpect(jsonPath("$.data[2].salesCount").value(30))
        .andExpect(jsonPath("$.data[2].popularityScore").value(47.0));
  }

  @Test
  @DisplayName("인기 상품 조회 - 판매량이 0인 경우도 정상 조회")
  void getPopularProducts_withZeroSales() throws Exception {
    // given
    for (int i = 0; i < 100; i++) {
      product1.increaseViewCount();
    }
    productRepository.save(product1);

    // when & then
    mockMvc.perform(get("/api/v1/products/popular"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].salesCount").value(0))
        .andExpect(jsonPath("$.data[0].viewCount").value(100));
  }

  private void createOrder(Long userId, Long productId, Long quantity) throws Exception {
    CartItem cartItem = CartItem.create(userId, productId, quantity);
    cartItem = cartItemRepository.save(cartItem);

    CartItemCouponMap cartItemCouponMap = new CartItemCouponMap(cartItem.getId(), null);
    CreateOrderRequest request = new CreateOrderRequest(userId, List.of(cartItemCouponMap));

    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }
}
