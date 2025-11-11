package com.phm.ecommerce.application.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Slf4j
@Component
public class InMemoryLockManager implements LockManager {

  private static final long LOCK_TIMEOUT_SECONDS = 10;

  // TODO: 메모리 해제 기능 추가
  private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

  @Override
  public <T> T executeWithLock(String lockKey, Supplier<T> task) {
    ReentrantLock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());

    try {
      boolean acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (!acquired) {
        throw new LockAcquisitionException("락 획득 시간 초과: " + lockKey);
      }

      try {
        log.debug("락 획득 완료: {}", lockKey);
        return task.get();
      } finally {
        lock.unlock();
        log.debug("락 해제 완료: {}", lockKey);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new LockAcquisitionException("락 획득 중 인터럽트 발생: " + lockKey, e);
    }
  }
}
