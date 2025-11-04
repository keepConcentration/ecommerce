package com.phm.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponErrorCode;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import com.phm.ecommerce.presentation.dto.request.IssueCouponRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("쿠폰 통합 테스트 (Controller + UseCase)")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CouponIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  private UserCouponRepository userCouponRepository;

  private Coupon testCoupon;
  private Long testUserId = 1L;

  @BeforeEach
  void setUp() {
    // 테스트용 쿠폰 생성
    testCoupon = Coupon.create("신규가입 쿠폰", 5000L, 10L, 30);
    testCoupon = couponRepository.save(testCoupon);
  }

  @Test
  @DisplayName("쿠폰 발급 - 성공")
  void issueCoupon_Success() throws Exception {
    // given
    IssueCouponRequest request = new IssueCouponRequest(testUserId);

    // when & then
    mockMvc.perform(post("/api/v1/coupons/{couponId}/issue", testCoupon.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.userId").value(testUserId))
        .andExpect(jsonPath("$.data.couponId").value(testCoupon.getId()))
        .andExpect(jsonPath("$.data.couponName").value("신규가입 쿠폰"))
        .andExpect(jsonPath("$.data.discountAmount").value(5000))
        .andExpect(jsonPath("$.data.issuedAt").exists())
        .andExpect(jsonPath("$.data.expiredAt").exists());
  }

  @Test
  @DisplayName("쿠폰 발급 - 실패 (존재하지 않는 쿠폰)")
  void issueCoupon_CouponNotFound() throws Exception {
    // given
    IssueCouponRequest request = new IssueCouponRequest(testUserId);

    // when & then
    mockMvc.perform(post("/api/v1/coupons/{couponId}/issue", 999L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(CouponErrorCode.COUPON_NOT_FOUND.getCode()));
  }

  @Test
  @DisplayName("쿠폰 발급 - 실패 (이미 발급받은 쿠폰)")
  void issueCoupon_AlreadyIssued() throws Exception {
    // given
    UserCoupon existingUserCoupon = UserCoupon.issue(testUserId, testCoupon.getId(), 30);
    userCouponRepository.save(existingUserCoupon);

    IssueCouponRequest request = new IssueCouponRequest(testUserId);

    // when & then
    mockMvc.perform(post("/api/v1/coupons/{couponId}/issue", testCoupon.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(CouponErrorCode.COUPON_ALREADY_ISSUED.getCode()));
  }

  @Test
  @DisplayName("쿠폰 발급 - 실패 (수량 소진)")
  void issueCoupon_SoldOut() throws Exception {
    // given - 수량이 0인 쿠폰 생성
    Coupon soldOutCoupon = Coupon.create("품절 쿠폰", 10000L, 0L, 30);
    soldOutCoupon = couponRepository.save(soldOutCoupon);

    IssueCouponRequest request = new IssueCouponRequest(testUserId);

    // when & then
    mockMvc.perform(post("/api/v1/coupons/{couponId}/issue", soldOutCoupon.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error.code").value(CouponErrorCode.COUPON_SOLD_OUT.getCode()));
  }

  @Test
  @DisplayName("사용자 쿠폰 조회 - 성공")
  void getUserCoupons_Success() throws Exception {
    // given
    UserCoupon userCoupon1 = UserCoupon.issue(testUserId, testCoupon.getId(), 30);
    userCouponRepository.save(userCoupon1);

    Coupon coupon2 = Coupon.create("추가 쿠폰", 3000L, 5L, 15);
    coupon2 = couponRepository.save(coupon2);
    UserCoupon userCoupon2 = UserCoupon.issue(testUserId, coupon2.getId(), 15);
    userCouponRepository.save(userCoupon2);

    // when & then
    mockMvc.perform(get("/api/v1/coupons")
            .param("userId", String.valueOf(testUserId))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("사용자 쿠폰 조회 - 빈 목록")
  void getUserCoupons_Empty() throws Exception {
    // given
    Long userWithNoCoupons = 999L;

    // when & then
    mockMvc.perform(get("/api/v1/coupons")
            .param("userId", String.valueOf(userWithNoCoupons))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }
}
