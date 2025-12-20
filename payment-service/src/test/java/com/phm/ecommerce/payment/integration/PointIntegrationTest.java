package com.phm.ecommerce.payment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.common.domain.point.Point;
import com.phm.ecommerce.common.infrastructure.repository.PointRepository;
import com.phm.ecommerce.payment.presentation.dto.request.ChargePointsRequest;
import com.phm.ecommerce.payment.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("포인트 통합 테스트 (Controller + UseCase)")
class PointIntegrationTest extends TestContainerSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private PointRepository pointRepository;

  private Long testUserId = 1L;

  @BeforeEach
  void setUp() {
    // 테스트용 포인트 생성 및 충전
    Point point = Point.create(testUserId);
    point.charge(10000L);
    pointRepository.save(point);
  }

  @Test
  @DisplayName("포인트 조회 - 성공")
  void getPoints_Success() throws Exception {
    // when & then
    mockMvc.perform(get("/api/v1/points")
            .param("userId", String.valueOf(testUserId))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.userId").value(testUserId))
        .andExpect(jsonPath("$.data.amount").value(10000));
  }

  @Test
  @DisplayName("포인트 조회 - 성공 (신규 사용자는 0원으로 자동 생성)")
  void getPoints_NewUser() throws Exception {
    // given
    Long newUserId = 999L;

    // when & then - 신규 사용자는 포인트가 0원으로 자동 생성됨
    mockMvc.perform(get("/api/v1/points")
            .param("userId", String.valueOf(newUserId))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.userId").value(newUserId))
        .andExpect(jsonPath("$.data.amount").value(0));
  }

  @Test
  @DisplayName("포인트 충전 - 성공")
  void chargePoints_Success() throws Exception {
    // given
    ChargePointsRequest request = new ChargePointsRequest(testUserId, 5000L);

    // when & then
    mockMvc.perform(post("/api/v1/points/charge")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data.userId").value(testUserId))
        .andExpect(jsonPath("$.data.amount").value(15000)) // 10000 + 5000
        .andExpect(jsonPath("$.data.chargedAmount").value(5000))
        .andExpect(jsonPath("$.data.pointTransactionId").exists());
  }

  @Test
  @DisplayName("포인트 충전 - 실패 (음수 금액)")
  void chargePoints_NegativeAmount() throws Exception {
    // given
    ChargePointsRequest request = new ChargePointsRequest(testUserId, -1000L);

    // when & then
    mockMvc.perform(post("/api/v1/points/charge")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(false));
  }

  @Test
  @DisplayName("포인트 충전 내역 조회 - 성공")
  void getPointTransactions_Success() throws Exception {
    // given - 포인트 충전하여 내역 생성
    ChargePointsRequest chargeRequest = new ChargePointsRequest(testUserId, 3000L);
    mockMvc.perform(post("/api/v1/points/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(chargeRequest)));

    // when & then
    mockMvc.perform(get("/api/v1/transactions")
            .param("userId", String.valueOf(testUserId))
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].amount").value(3000));
  }
}
