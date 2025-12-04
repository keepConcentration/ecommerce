package com.phm.ecommerce.application.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class MultiLockManager {

    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "lock:";
    private static final long WAIT_TIME_SECONDS = 10;
    private static final long LEASE_TIME_SECONDS = 5;

    public <T> T executeWithLocks(List<String> sortedLockKeys, Supplier<T> task) {
        if (sortedLockKeys == null || sortedLockKeys.isEmpty()) {
            log.warn("락 키 목록이 비어있음 - 락 없이 작업 실행");
            return task.get();
        }

        log.debug("MultiLock 획득 시도 - lockCount: {}, keys: {}", sortedLockKeys.size(), sortedLockKeys);

        RLock[] locks = sortedLockKeys.stream()
                .map(key -> redissonClient.getLock(LOCK_PREFIX + key))
                .toArray(RLock[]::new);

        RedissonMultiLock multiLock = new RedissonMultiLock(locks);
        boolean acquired = false;

        try {
            acquired = multiLock.tryLock(
                    WAIT_TIME_SECONDS,
                    LEASE_TIME_SECONDS,
                    TimeUnit.SECONDS
            );

            if (!acquired) {
                log.warn("MultiLock 획득 실패 - lockKeys: {}", sortedLockKeys);
                throw new LockAcquisitionException("여러 락 획득 시간 초과: " + sortedLockKeys);
            }

            log.debug("MultiLock 획득 성공 - lockCount: {}", locks.length);

            return task.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("MultiLock 획득 중 인터럽트 - lockKeys: {}", sortedLockKeys, e);
            throw new LockAcquisitionException("락 획득 중 인터럽트 발생: " + sortedLockKeys);
        } finally {
            if (acquired) {
                try {
                    multiLock.unlock();
                    log.debug("MultiLock 해제 완료 - lockCount: {}", locks.length);
                } catch (IllegalMonitorStateException e) {
                    log.warn("MultiLock 해제 시 예외 발생 - lockKeys: {}", sortedLockKeys);
                }
            }
        }
    }

    public <T> T executeWithSortedLocks(List<String> lockKeys, Supplier<T> task) {
        if (lockKeys == null || lockKeys.isEmpty()) {
            log.warn("락 키 목록이 비어있음 - 락 없이 작업 실행");
            return task.get();
        }

        List<String> sortedKeys = lockKeys.stream()
                .distinct()
                .sorted()
                .toList();

        log.debug("락 키 정렬 완료 - original: {}, sorted: {}", lockKeys, sortedKeys);

        return executeWithLocks(sortedKeys, task);
    }
}
