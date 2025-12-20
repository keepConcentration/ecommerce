package com.phm.ecommerce.promotion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.domain.coupon.Coupon;
import com.phm.ecommerce.common.domain.user.User;
import com.phm.ecommerce.promotion.infrastructure.cache.RedisCacheKeys;
import com.phm.ecommerce.promotion.application.service.CouponIssueBatchService;
import com.phm.ecommerce.promotion.infrastructure.queue.CouponQueueService;
import com.phm.ecommerce.common.infrastructure.repository.CouponRepository;
import com.phm.ecommerce.common.infrastructure.repository.UserCouponRepository;
import com.phm.ecommerce.common.infrastructure.repository.UserRepository;
import com.phm.ecommerce.promotion.presentation.dto.request.IssueCouponRequest;
import com.phm.ecommerce.promotion.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("비동기 쿠폰 발급 통합 테스트")
class AsyncCouponIssueIntegrationTest extends TestContainerSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserCouponRepository userCouponRepository;

  @Autowired
  private CouponQueueService couponQueueService;

  @Autowired
  private CouponIssueBatchService couponIssueBatchService;

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  private Coupon coupon;
  private List<User> users;

  @BeforeEach
  void setUp() {
    redisTemplate.delete(redisTemplate.keys(RedisCacheKeys.couponQueue(1L) + "*"));

    coupon = Coupon.create("선착순 쿠폰", 5000L, 10L, 30);
    coupon = couponRepository.save(coupon);

    users = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      User user = User.create();
      users.add(userRepository.save(user));
    }
  }

  @Test
  @DisplayName("비동기 쿠폰 발급 - 중복 요청")
  void requestCouponIssue_duplicateRequest() throws Exception {
    // given
    User user = users.getFirst();
    IssueCouponRequest request = new IssueCouponRequest(user.getId());

    mockMvc.perform(post("/api/v1/coupons/{couponId}/request", coupon.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isAccepted());

    // when
    mockMvc.perform(post("/api/v1/coupons/{couponId}/request", coupon.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(false))
        .andExpect(jsonPath("$.error").exists());

    // then
    long queueSize = couponQueueService.getQueueSize(coupon.getId());
    assertThat(queueSize).isEqualTo(1L);
  }

  @Test
  @DisplayName("비동기 쿠폰 발급 - 100명 요청, 배치 처리, 10명만 발급")
  void requestCouponIssue_100Users_then_batch() throws Exception {
    // given
    for (int i = 0; i < 100; i++) {
      IssueCouponRequest request = new IssueCouponRequest(users.get(i).getId());
      mockMvc.perform(post("/api/v1/coupons/{couponId}/request", coupon.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isAccepted());
    }

    // then
    long queueSizeBeforeBatch = couponQueueService.getQueueSize(coupon.getId());
    assertThat(queueSizeBeforeBatch).isEqualTo(100L);

    // when
    CouponIssueBatchService.ProcessResult result = couponIssueBatchService.processCouponQueue(coupon.getId());

    // then
    assertThat(result.successCount()).isEqualTo(10);
    assertThat(result.failCount()).isEqualTo(0);

    long issuedCount = userCouponRepository.countByCouponId(coupon.getId());
    assertThat(issuedCount).isEqualTo(10L);

    Coupon updatedCoupon = couponRepository.findByIdOrThrow(coupon.getId());
    assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(10L);
    assertThat(updatedCoupon.getRemainingQuantity()).isEqualTo(0L);

    long queueSizeAfterBatch = couponQueueService.getQueueSize(coupon.getId());
    assertThat(queueSizeAfterBatch).isEqualTo(0L);
  }

  @Test
  @DisplayName("배치 처리 - FIFO")
  void batchProcessing_fifoOrder() throws Exception {
    // given
    List<User> requestedUsers = users.subList(0, 20);

    for (User user : requestedUsers) {
      IssueCouponRequest request = new IssueCouponRequest(user.getId());
      mockMvc.perform(post("/api/v1/coupons/{couponId}/request", coupon.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // 요청 간 대기 시간
      Thread.sleep(10);
    }

    // when
    couponIssueBatchService.processCouponQueue(coupon.getId());

    // then
    for (int i = 0; i < 10; i++) {
      boolean issued = userCouponRepository.existsByUserIdAndCouponId(
          requestedUsers.get(i).getId(), coupon.getId());
      assertThat(issued).as("User %d should have coupon", i).isTrue();
    }

    for (int i = 10; i < 20; i++) {
      boolean issued = userCouponRepository.existsByUserIdAndCouponId(
          requestedUsers.get(i).getId(), coupon.getId());
      assertThat(issued).as("User %d should NOT have coupon", i).isFalse();
    }
  }
}
