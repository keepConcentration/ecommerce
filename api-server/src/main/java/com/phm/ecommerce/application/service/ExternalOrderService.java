package com.phm.ecommerce.application.service;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalOrderService {

  public void sendOrderToExternalSystem(Long orderId, Long userId, Long finalAmount, LocalDateTime createdAt) {
    try {
      log.info("외부 시스템으로 주문 정보 전송 시작 - orderId: {}, userId: {}, finalAmount: {}, createdAt: {}",
          orderId, userId, finalAmount, createdAt);

      Thread.sleep(100);

      log.info("외부 시스템으로 주문 정보 전송 완료 - orderId: {}", orderId);
    } catch (Exception e) {
      log.error("외부 시스템으로 주문 정보 전송 실패 - orderId: {}, error: {}",
          orderId, e.getMessage());
    }
  }
}
