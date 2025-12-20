package com.phm.ecommerce.common.application.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "lock:";

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = resolveLockKey(joinPoint, distributedLock);

        RLock lock = distributedLock.fair()
                ? redissonClient.getFairLock(lockKey)
                : redissonClient.getLock(lockKey);

        if (distributedLock.fair()) {
            log.debug("Fair Lock 사용 - lockKey: {}", lockKey);
        }

        try {
            boolean acquired = lock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!acquired) {
                throw new LockAcquisitionException("락 획득 시간 초과: " + lockKey);
            }
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("락 획득 중 인터럽트 발생: " + lockKey);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String resolveLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        if (!distributedLock.lockKeyProvider().isEmpty()) {
            String lockKeyWithoutPrefix = invokeLockKeyProvider(joinPoint, distributedLock.lockKeyProvider());
            return LOCK_PREFIX + lockKeyWithoutPrefix;
        }

        throw new LockAcquisitionException("@DistributedLock에 lockKeyProvider가 지정되지 않았습니다.");
    }

    private String invokeLockKeyProvider(ProceedingJoinPoint joinPoint, String providerMethodName) {
        try {
            Object target = joinPoint.getTarget();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();

            Method providerMethod = target.getClass()
                    .getDeclaredMethod(providerMethodName, signature.getMethod().getParameterTypes());
            providerMethod.setAccessible(true);

            Object result = providerMethod.invoke(target, args);

            if (!(result instanceof String)) {
                throw new LockAcquisitionException(
                        "락 키 제공 메서드는 String을 반환해야 합니다: " + providerMethodName);
            }

            return (String) result;

        } catch (NoSuchMethodException e) {
            throw new LockAcquisitionException(
                    "락 키 제공 메서드를 찾을 수 없습니다: " + providerMethodName, e);
        } catch (Exception e) {
            throw new LockAcquisitionException(
                    "락 키 제공 메서드 호출 중 오류 발생: " + providerMethodName, e);
        }
    }
}
