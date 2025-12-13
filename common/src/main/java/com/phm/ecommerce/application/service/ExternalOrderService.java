package com.phm.ecommerce.application.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// TODO 적절한 위치로 클래스 이동
@Slf4j
@Service
public class ExternalOrderService {

  private static final SecureRandom random = new SecureRandom();

  public void sendOrderToExternalSystem(Long orderId, Long userId, Long finalAmount, LocalDateTime createdAt)
      throws InterruptedException {
    log.info("외부 시스템으로 주문 정보 전송 시작 - orderId: {}, userId: {}, finalAmount: {}, createdAt: {}",
        orderId, userId, finalAmount, createdAt);

    Thread.sleep(100);

    // 50% 확률로 예외 발생
    if (random.nextBoolean()) {
      log.error("외부 시스템 오류 발생 (랜덤) - orderId: {}", orderId);
      throw new RuntimeException("외부 시스템 통신 오류");
    }

    log.info("외부 시스템으로 주문 정보 전송 완료 - orderId: {}", orderId);
  }
}
