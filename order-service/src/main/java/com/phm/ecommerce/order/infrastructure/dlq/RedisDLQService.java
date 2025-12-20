package com.phm.ecommerce.order.infrastructure.dlq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisDLQService {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper redisObjectMapper;
  private final DLQProperties dlqProperties;

  private static final String DLQ_SORTED_SET_KEY = "dlq:retry:queue";
  private static final String DLQ_HASH_KEY = "dlq:messages";

  public void addToDeadLetterQueue(DeadLetterMessage message) {
    try {
      LocalDateTime nextRetryAt = calculateNextRetryAt(message.retryCount());
      DeadLetterMessage messageWithRetryTime = message.withNextRetryAt(nextRetryAt);

      String messageJson = redisObjectMapper.writeValueAsString(messageWithRetryTime);
      double score = toScore(nextRetryAt);

      redisTemplate.opsForZSet().add(DLQ_SORTED_SET_KEY, messageWithRetryTime.id(), score);
      redisTemplate.opsForHash().put(DLQ_HASH_KEY, messageWithRetryTime.id(), messageJson);

      log.info("DLQ 저장 완료 - id: {}, retryCount: {}, nextRetryAt: {}",
          messageWithRetryTime.id(), messageWithRetryTime.retryCount(), nextRetryAt);

    } catch (JsonProcessingException e) {
      log.error("DLQ 저장 실패 - JSON 직렬화 오류: id={}", message.id(), e);
      throw new RuntimeException("DLQ 저장 실패", e);
    }
  }

  public List<DeadLetterMessage> getRetryableMessages() {
    try {
      double now = toScore(LocalDateTime.now());

      // Sorted Set에서 messageId 목록 조회
      Set<String> messageIds = redisTemplate.opsForZSet()
          .rangeByScore(DLQ_SORTED_SET_KEY, 0, now);

      if (messageIds == null || messageIds.isEmpty()) {
        return List.of();
      }

      // Hash에서 실제 메시지 조회
      List<DeadLetterMessage> messages = new ArrayList<>();
      for (String messageId : messageIds) {
        String json = (String) redisTemplate.opsForHash().get(DLQ_HASH_KEY, messageId);
        if (json != null) {
          try {
            DeadLetterMessage message = redisObjectMapper.readValue(json, DeadLetterMessage.class);
            messages.add(message);
          } catch (JsonProcessingException e) {
            log.error("DLQ 메시지 파싱 실패 - messageId: {}", messageId, e);
          }
        }
      }

      log.debug("재시도 가능 메시지 조회 완료 - 개수: {}", messages.size());
      return messages;

    } catch (Exception e) {
      log.error("재시도 가능 메시지 조회 실패", e);
      return List.of();
    }
  }

  public boolean removeMessage(String messageId) {
    try {
      Long zsetRemoved = redisTemplate.opsForZSet().remove(DLQ_SORTED_SET_KEY, messageId);
      Long hashRemoved = redisTemplate.opsForHash().delete(DLQ_HASH_KEY, messageId);

      boolean success = (zsetRemoved != null && zsetRemoved > 0) ||
          (hashRemoved != null && hashRemoved > 0);

      if (success) {
        log.info("DLQ 메시지 제거 완료 - id: {}", messageId);
      } else {
        log.warn("DLQ 메시지 제거 실패 - 메시지를 찾을 수 없음: id={}", messageId);
      }
      return success;

    } catch (Exception e) {
      log.error("DLQ 메시지 제거 중 오류 - id: {}", messageId, e);
      return false;
    }
  }

  public void updateMessage(DeadLetterMessage message) {
    try {
      // nextRetryAt 재계산
      LocalDateTime nextRetryAt = calculateNextRetryAt(message.retryCount());
      DeadLetterMessage updatedMessage = message.withNextRetryAt(nextRetryAt);

      String messageJson = redisObjectMapper.writeValueAsString(updatedMessage);
      double score = toScore(nextRetryAt);

      redisTemplate.opsForZSet().add(DLQ_SORTED_SET_KEY, updatedMessage.id(), score);
      redisTemplate.opsForHash().put(DLQ_HASH_KEY, updatedMessage.id(), messageJson);

      log.info("DLQ 메시지 업데이트 완료 - id: {}, retryCount: {}, nextRetryAt: {}",
          updatedMessage.id(), updatedMessage.retryCount(), nextRetryAt);

    } catch (JsonProcessingException e) {
      log.error("DLQ 메시지 업데이트 실패 - id: {}", message.id(), e);
      throw new RuntimeException("DLQ 메시지 업데이트 실패", e);
    }
  }

  public boolean canRetry(DeadLetterMessage message) {
    return message.retryCount() < dlqProperties.getMaxRetryCount();
  }

  public LocalDateTime calculateNextRetryAt(int retryCount) {
    long delayMinutes = (long) (
        dlqProperties.getRetryIntervalMinutes() *
            Math.pow(dlqProperties.getBackoffMultiplier(), retryCount)
    );
    return LocalDateTime.now().plusMinutes(delayMinutes);
  }

  private double toScore(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
}
