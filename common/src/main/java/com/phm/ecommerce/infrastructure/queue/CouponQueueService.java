package com.phm.ecommerce.infrastructure.queue;

import com.phm.ecommerce.infrastructure.cache.RedisCacheKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponQueueService {

  private final RedisTemplate<String, Object> redisTemplate;

  public boolean addCouponRequest(Long couponId, Long userId) {
    String queueKey = RedisCacheKeys.couponQueue(couponId);
    String member = createMember(userId, couponId);

    String sequenceKey = RedisCacheKeys.couponQueueSequence(couponId);
    Long sequence = redisTemplate.opsForValue().increment(sequenceKey);

    if (sequence == null) {
      log.error("쿠폰 발급 요청 시퀀스 생성 실패 - couponId: {}, userId: {}", couponId, userId);
      return false;
    }

    Boolean added = redisTemplate.opsForZSet().add(queueKey, member, sequence.doubleValue());

    if (Boolean.TRUE.equals(added)) {
      log.debug("쿠폰 발급 요청 큐 추가 성공 - couponId: {}, userId: {}, sequence: {}",
          couponId, userId, sequence);
      return true;
    } else {
      log.debug("쿠폰 발급 요청 중복 - couponId: {}, userId: {}", couponId, userId);
      return false;
    }
  }

  public boolean existsInQueue(Long couponId, Long userId) {
    String queueKey = RedisCacheKeys.couponQueue(couponId);
    String member = createMember(userId, couponId);

    Double score = redisTemplate.opsForZSet().score(queueKey, member);
    return score != null;
  }

  public Set<ZSetOperations.TypedTuple<Object>> popOldestRequests(Long couponId, long limit) {
    String queueKey = RedisCacheKeys.couponQueue(couponId);

    Set<ZSetOperations.TypedTuple<Object>> requests = redisTemplate.opsForZSet()
        .popMin(queueKey, limit);

    if (requests != null && !requests.isEmpty()) {
      log.debug("쿠폰 발급 큐에서 {} 개 요청 조회 - couponId: {}", requests.size(), couponId);
    }

    return requests;
  }

  public long getQueueSize(Long couponId) {
    String queueKey = RedisCacheKeys.couponQueue(couponId);
    Long size = redisTemplate.opsForZSet().zCard(queueKey);
    return size != null ? size : 0L;
  }

  public long removeCouponRequest(Long couponId, Long userId) {
    String queueKey = RedisCacheKeys.couponQueue(couponId);
    String member = createMember(userId, couponId);

    Long removed = redisTemplate.opsForZSet().remove(queueKey, member);
    return removed != null ? removed : 0L;
  }

  private String createMember(Long userId, Long couponId) {
    return userId + ":" + couponId;
  }

  public Long extractUserId(String member) {
    String[] parts = member.split(":");
    return Long.parseLong(parts[0]);
  }

  public Long extractCouponId(String member) {
    String[] parts = member.split(":");
    return Long.parseLong(parts[1]);
  }

  public void clearQueue(Long couponId) {
    String queueKey = RedisCacheKeys.couponQueue(couponId);
    String sequenceKey = RedisCacheKeys.couponQueueSequence(couponId);

    Long queueSize = redisTemplate.opsForZSet().zCard(queueKey);
    redisTemplate.delete(queueKey);
    redisTemplate.delete(sequenceKey);

    if (queueSize != null && queueSize > 0) {
      log.info("쿠폰 대기 큐 클리어 - couponId: {}, 삭제된 요청 수: {}", couponId, queueSize);
    }
  }
}
