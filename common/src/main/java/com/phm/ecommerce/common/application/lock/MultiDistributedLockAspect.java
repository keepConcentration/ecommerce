package com.phm.ecommerce.common.application.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class MultiDistributedLockAspect {

    private final MultiLockManager multiLockManager;

    @Around("@annotation(multiDistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, MultiDistributedLock multiDistributedLock) throws Throwable {

        List<String> lockKeys = invokeLockKeyProvider(joinPoint, multiDistributedLock.lockKeyProvider());

        if (lockKeys == null || lockKeys.isEmpty()) {
            log.warn("락 키가 비어있음 - 락 없이 메서드 실행: {}", joinPoint.getSignature().toShortString());
            return joinPoint.proceed();
        }

        List<String> prefixedLockKeys = lockKeys.stream()
                .map(key -> "lock:" + key)
                .toList();

        log.debug("MultiDistributedLock 적용 - method: {}, lockKeyCount: {}, lockKeys: {}",
                joinPoint.getSignature().toShortString(), prefixedLockKeys.size(), prefixedLockKeys);

        return multiLockManager.executeWithSortedLocks(prefixedLockKeys, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new LockAcquisitionException("메서드 실행 중 오류 발생", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<String> invokeLockKeyProvider(ProceedingJoinPoint joinPoint, String providerMethodName) {
        try {
            Object target = joinPoint.getTarget();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();

            Method providerMethod = target.getClass()
                    .getDeclaredMethod(providerMethodName, signature.getMethod().getParameterTypes());
            providerMethod.setAccessible(true);

            Object result = providerMethod.invoke(target, args);

            if (!(result instanceof List)) {
                throw new LockAcquisitionException(
                        "락 키 제공 메서드는 List<String>을 반환해야 합니다: " + providerMethodName);
            }

            return (List<String>) result;

        } catch (NoSuchMethodException e) {
            throw new LockAcquisitionException(
                    "락 키 제공 메서드를 찾을 수 없습니다: " + providerMethodName, e);
        } catch (Exception e) {
            throw new LockAcquisitionException(
                    "락 키 제공 메서드 호출 중 오류 발생: " + providerMethodName, e);
        }
    }
}
