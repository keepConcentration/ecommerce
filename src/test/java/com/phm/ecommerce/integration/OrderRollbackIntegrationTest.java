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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("주문 롤백 통합 테스트")
class OrderRollbackIntegrationTest extends TestContainerSupport {

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
  private Product product;

  @BeforeEach
  void setUp() {
    // 사용자 생성
    user = User.create();
    user = userRepository.save(user);

    // 포인트 생성
    Point point = Point.create(user.getId());
    point.charge(10000L);
    pointRepository.save(point);

    // 상품 생성
    product = Product.create("노트북", 1500000L, 100L);
    product = productRepository.save(product);
  }

  @Test
  @DisplayName("주문 실패 시 판매량이 롤백되어야 함 (포인트 부족)")
  void shouldRollbackSalesCount_whenOrderFails_dueToInsufficientPoints() throws Exception {
    // given
    Long orderQuantity = 10L;
    CartItem cartItem = CartItem.create(user.getId(), product.getId(), orderQuantity);
    cartItem = cartItemRepository.save(cartItem);

    CartItemCouponMap cartItemCouponMap = new CartItemCouponMap(cartItem.getId(), null);
    CreateOrderRequest request = new CreateOrderRequest(user.getId(), List.of(cartItemCouponMap));

    // when
    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());

    // then
    Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
    assertThat(updatedProduct.getSalesCount()).isEqualTo(0L);
    assertThat(updatedProduct.getQuantity()).isEqualTo(100L);
  }
}
