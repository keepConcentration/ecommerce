package com.phm.ecommerce.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.domain.cart.CartItem;
import com.phm.ecommerce.common.domain.cart.exception.CartErrorCode;
import com.phm.ecommerce.common.domain.product.Product;
import com.phm.ecommerce.common.domain.product.exception.ProductErrorCode;
import com.phm.ecommerce.common.infrastructure.repository.CartItemRepository;
import com.phm.ecommerce.common.infrastructure.repository.ProductRepository;
import com.phm.ecommerce.order.presentation.dto.request.AddCartItemRequest;
import com.phm.ecommerce.order.presentation.dto.request.DeleteCartItemRequest;
import com.phm.ecommerce.order.presentation.dto.request.UpdateQuantityRequest;
import com.phm.ecommerce.order.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@AutoConfigureMockMvc
@Transactional
@DisplayName("장바구니 통합 테스트 (Controller + UseCase)")
class CartIntegrationTest extends TestContainerSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CartItemRepository cartItemRepository;

  @Autowired
  private ProductRepository productRepository;

  private Product testProduct;
  private Long testUserId = 1L;

  @BeforeEach
  void setUp() {
    // 테스트용 상품 생성
    testProduct = Product.create("테스트 상품", 10000L, 100L);
    testProduct = productRepository.save(testProduct);
  }

  @Test
  @DisplayName("장바구니에 상품 추가 - 성공")
  void addCartItem_Success() throws Exception {
    // given
    AddCartItemRequest request = new AddCartItemRequest(testUserId, testProduct.getId(), 5L);

    // when & then
    mockMvc.perform(put("/api/v1/cart/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.productId").value(testProduct.getId()))
        .andExpect(jsonPath("$.data.productName").value("테스트 상품"))
        .andExpect(jsonPath("$.data.price").value(10000))
        .andExpect(jsonPath("$.data.quantity").value(5));
  }

  @Test
  @DisplayName("장바구니에 동일 상품 추가 - 수량 증가")
  void addCartItem_IncreaseQuantity() throws Exception {
    // given
    CartItem existingCartItem = CartItem.create(testUserId, testProduct.getId(), 3L);
    cartItemRepository.save(existingCartItem);

    AddCartItemRequest request = new AddCartItemRequest(testUserId, testProduct.getId(), 2L);

    // when & then
    mockMvc.perform(put("/api/v1/cart/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.quantity").value(5)); // 3 + 2
  }

  @Test
  @DisplayName("장바구니에 상품 추가 - 실패 (존재하지 않는 상품)")
  void addCartItem_ProductNotFound() throws Exception {
    // given
    AddCartItemRequest request = new AddCartItemRequest(testUserId, 999L, 5L);

    // when & then
    mockMvc.perform(put("/api/v1/cart/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(ProductErrorCode.PRODUCT_NOT_FOUND.getCode()));
  }

  @Test
  @DisplayName("장바구니 조회 - 성공")
  void getCartItems_Success() throws Exception {
    // given
    CartItem cartItem1 = CartItem.create(testUserId, testProduct.getId(), 2L);
    cartItemRepository.save(cartItem1);

    Product product2 = Product.create("상품2", 20000L, 50L);
    product2 = productRepository.save(product2);
    CartItem cartItem2 = CartItem.create(testUserId, product2.getId(), 3L);
    cartItemRepository.save(cartItem2);

    // when & then
    mockMvc.perform(get("/api/v1/cart/items")
            .param("userId", String.valueOf(testUserId))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(jsonPath("$.data.items.length()").value(2));
  }

  @Test
  @DisplayName("장바구니 상품 수량 수정 - 성공")
  void updateCartItemQuantity_Success() throws Exception {
    // given
    CartItem cartItem = CartItem.create(testUserId, testProduct.getId(), 3L);
    CartItem savedCartItem = cartItemRepository.save(cartItem);

    UpdateQuantityRequest request = new UpdateQuantityRequest(10L);

    // when & then
    mockMvc.perform(patch("/api/v1/cart/items/{cartItemId}", savedCartItem.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.quantity").value(10));
  }

  @Test
  @DisplayName("장바구니 상품 수량 수정 - 실패 (존재하지 않는 장바구니 아이템)")
  void updateCartItemQuantity_NotFound() throws Exception {
    // given
    UpdateQuantityRequest request = new UpdateQuantityRequest(10L);

    // when & then
    mockMvc.perform(patch("/api/v1/cart/items/{cartItemId}", 999L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(CartErrorCode.CART_ITEM_NOT_FOUND.getCode()));
  }

  @Test
  @DisplayName("장바구니 상품 삭제 - 성공")
  void deleteCartItem_Success() throws Exception {
    // given
    DeleteCartItemRequest request = new DeleteCartItemRequest(testUserId);
    CartItem cartItem = CartItem.create(testUserId, testProduct.getId(), 3L);
    CartItem savedCartItem = cartItemRepository.save(cartItem);

    // when & then
    mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", savedCartItem.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNoContent());

    // 삭제 확인
    assertThat(cartItemRepository.findById(savedCartItem.getId())).isEmpty();
  }

  @Test
  @DisplayName("장바구니 상품 삭제 - 실패 (존재하지 않는 장바구니 아이템)")
  void deleteCartItem_NotFound() throws Exception {
    // given
    DeleteCartItemRequest request = new DeleteCartItemRequest(testUserId);

    // when & then
    mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", 999L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(CartErrorCode.CART_ITEM_NOT_FOUND.getCode()));
  }

  @Test
  @DisplayName("장바구니 상품 삭제 - 실패 (다른 사용자의 장바구니 아이템)")
  void deleteCartItem_OwnershipViolation() throws Exception {
    // given
    Long ownerUserId = 1L;
    Long otherUserId = 2L;

    CartItem cartItem = CartItem.create(ownerUserId, testProduct.getId(), 3L);
    CartItem savedCartItem = cartItemRepository.save(cartItem);

    DeleteCartItemRequest request = new DeleteCartItemRequest(otherUserId);

    // when & then
    mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", savedCartItem.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(CartErrorCode.CART_ITEM_OWNERSHIP_VIOLATION.getCode()));

    assertThat(cartItemRepository.findById(savedCartItem.getId())).isPresent();
  }
}
