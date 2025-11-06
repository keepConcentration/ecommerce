package com.phm.ecommerce.integration;

import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.product.exception.ProductErrorCode;
import com.phm.ecommerce.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("상품 통합 테스트 (Controller + UseCase)")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  @Test
  @DisplayName("상품 목록 조회 - 성공")
  void getProducts_Success() throws Exception {
    // given
    Product product1 = Product.create("노트북", 1500000L, 10L);
    Product product2 = Product.create("마우스", 50000L, 20L);
    productRepository.save(product1);
    productRepository.save(product2);

    // when & then
    mockMvc.perform(get("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].name").value("노트북"))
        .andExpect(jsonPath("$.data[0].price").value(1500000))
        .andExpect(jsonPath("$.data[1].name").value("마우스"))
        .andExpect(jsonPath("$.data[1].price").value(50000));
  }

  @Test
  @DisplayName("상품 단건 조회 - 성공 (조회수 증가)")
  void getProductById_Success() throws Exception {
    // given
    Product product = Product.create("노트북", 1500000L, 10L);
    Product savedProduct = productRepository.save(product);
    Long initialViewCount = savedProduct.getViewCount();

    // when & then
    mockMvc.perform(get("/api/v1/products/{productId}", savedProduct.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.productId").value(savedProduct.getId()))
        .andExpect(jsonPath("$.data.name").value("노트북"))
        .andExpect(jsonPath("$.data.price").value(1500000))
        .andExpect(jsonPath("$.data.quantity").value(10))
        .andExpect(jsonPath("$.data.viewCount").value(initialViewCount + 1));
  }

  @Test
  @DisplayName("상품 단건 조회 - 실패 (존재하지 않는 상품)")
  void getProductById_NotFound() throws Exception {
    // given
    Long nonExistentProductId = 999L;

    // when & then
    mockMvc.perform(get("/api/v1/products/{productId}", nonExistentProductId)
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(ProductErrorCode.PRODUCT_NOT_FOUND.getCode()));
  }

  @Test
  @DisplayName("인기 상품 조회 - 성공")
  void getPopularProducts_Success() throws Exception {
    // when & then
    mockMvc.perform(get("/api/v1/products/popular")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }
}
