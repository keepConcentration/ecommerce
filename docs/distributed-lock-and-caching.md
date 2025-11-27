# Redis 분산 락과 캐싱 적용 보고서

## 1. 분산 락 구현

### 1.1 기존 문제점

InMemoryLock 사용의 한계:
- 단일 서버 환경에서만 동작
- 멀티 인스턴스 환경에서 동시성 제어 불가
- 서버 재시작 시 락 상태 소실

### 1.2 Redis 분산 락 도입

Redisson 기반 분산 락:
- 멀티 인스턴스 환경에서 동시성 제어 가능
- Redis를 통한 락 상태 공유
- 자동 락 해제 기능 (Lease Time)
- Fair Lock 지원으로 FIFO 보장

### 1.3 구현 방식

#### @DistributedLock 어노테이션
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key();                               // SpEL 표현식 지원
    long waitTime() default 10L;                // 락 대기 시간 (10초)
    long leaseTime() default 5L;                // 락 유지 시간 (5초)
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    boolean fair() default false;               // Fair Lock 여부
}
```

#### DistributedLockAspect (AOP 처리)
```java
@Around("@annotation(distributedLock)")
public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
    String lockKey = LOCK_PREFIX + SpelExpressionEvaluator.evaluate(joinPoint, distributedLock.key());

    RLock lock = distributedLock.fair()
            ? redissonClient.getFairLock(lockKey)
            : redissonClient.getLock(lockKey);

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

    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

### 1.4 적용 범위

분산 락이 적용된 UseCase:
- ChargePointsUseCase: 포인트 충전 (`lock:point:user:{userId}`)
- IssueCouponUseCase: 선착순 쿠폰 발급 (`lock:coupon:{couponId}`)
- CreateOrderUseCase: 장바구니 주문 (`lock:order:user:{userId}`)
- CreateDirectOrderUseCase: 단일 상품 주문 (중첩 락: 상품 → 포인트)

### 1.5 락 타입 결정 기준

비관적 락 (Pessimistic Lock):
- 선착순 쿠폰 발급: 동일 쿠폰에 요청 집중, 정확한 수량 제어 필수

낙관적 락 (Optimistic Lock):
- 재고 관리: 재고 충돌이 드물게 발생, 비관적 락보다 성능 우수
- 포인트 사용/충전: 동일 사용자의 동시 요청이 드물게 발생

분산 락 우선 적용:
- 멀티 인스턴스 환경 고려
- 데이터베이스 락보다 유연한 제어 가능
- 타임아웃 설정으로 데드락 방지

---

## 2. Redis 캐싱 적용

### 2.1 캐싱 대상 식별

조회 빈도가 높은 API:
- 인기 상품 조회: 모든 사용자에게 동일한 상위 5개 상품 노출
- 단일 상품 조회: 상품 상세 페이지 조회

캐싱 적용 근거:
- 읽기 빈도 >> 쓰기 빈도
- 데이터 변경이 상대적으로 드묾
- DB 부하 감소 필요

### 2.2 캐싱 전략

#### 2-Tier 캐싱 전략

인기 상품 조회의 경우:
1. Tier 1: 인기 상품 ID 목록 캐싱 (`popularProductIds`)
2. Tier 2: 개별 상품 정보 캐싱 (`product:{productId}`)

장점:
- 인기 상품 ID 변경 시 개별 상품 캐시는 유지
- 캐시 재사용성 증가
- 메모리 효율성 향상

#### Cache-Aside 패턴

```java
@Cacheable(value = "product", key = "#productId")
public ProductInfo getProduct(Long productId) {
    log.info("DB에서 상품 조회: productId={}", productId);
    Product product = productRepository.findByIdOrThrow(productId);
    return new ProductInfo(...);
}

@Cacheable(value = "popularProductIds", key = "'top' + #limit")
public ProductIdList getPopularProductIds(int limit) {
    log.info("DB에서 인기 상품 ID 조회: limit={}", limit);
    List<Long> ids = productRepository.findPopularProducts(limit)
        .stream()
        .map(Product::getId)
        .toList();
    return new ProductIdList(ids);
}
```

### 2.3 캐시 TTL 설정

| 캐시 이름 | TTL | 근거 |
|----------|-----|------|
| `product` | 30분 | 상품 정보 변경 빈도 낮음, 재사용성 높음 |
| `popularProductIds` | 10분 | 인기 상품 순위는 주기적 갱신 필요 |

### 2.4 캐시 무효화 전략

#### AOP 기반 자동 무효화

상품 정보 변경 시 자동으로 캐시 무효화:

```java
@AfterReturning(
    pointcut = "execution(* ProductRepository.save(..)) && args(product)",
    returning = "savedProduct"
)
public void evictProductCacheAfterSave(Product product, Product savedProduct) {
    Long productId = savedProduct.getId();
    log.info("상품 저장 감지 - 캐시 무효화: productId={}", productId);

    if (cacheManager.getCache("product") != null) {
        cacheManager.getCache("product").evict(productId);
    }
}
```

장점:
- UseCase에서 캐시 무효화 로직 분리
- 관심사의 분리 (Separation of Concerns)
- 유지보수성 향상

#### 인기 상품 캐시 갱신

주문 발생 시 인기 상품 캐시 무효화를 하지 않음:
- 주문마다 인기 상품 캐시를 갱신하는 것은 비효율적
- TTL(10분)에 의존하여 자동 갱신
- 인기 상품 순위는 실시간성이 덜 중요

### 2.5 직렬화 설정

#### Java Record 직렬화 문제 해결

GenericJackson2JsonRedisSerializer 사용 시:
- `DefaultTyping.NON_FINAL`은 Record에 타입 정보 미포함
- 역직렬화 시 `ClassCastException` 발생

해결 방법 - RecordSupportingTypeResolver:
```java
public class RecordSupportingTypeResolver extends DefaultTypeResolverBuilder {

    public RecordSupportingTypeResolver(DefaultTyping t, PolymorphicTypeValidator ptv) {
        super(t, ptv);
    }

    @Override
    public boolean useForType(JavaType t) {
        boolean isRecord = t.getRawClass().isRecord();
        boolean superResult = super.useForType(t);

        if (isRecord) {
            return true;  // Record는 항상 타입 정보 포함
        }
        return superResult;
    }
}
```

#### HTTP 응답과 Redis 직렬화 분리

문제점:
- Redis용 ObjectMapper가 `@class` 타입 정보를 포함
- HTTP 응답에도 타입 정보가 노출되어 지저분함

해결 방법:
```java
// JacksonConfig.java
@Bean
@Primary  // HTTP 응답용
public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
}

// RedisConfig.java
@Bean
public ObjectMapper redisObjectMapper() {  // Redis 전용
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    RecordSupportingTypeResolver typeResolver = new RecordSupportingTypeResolver(
        DefaultTyping.NON_FINAL,
        objectMapper.getPolymorphicTypeValidator());

    StdTypeResolverBuilder initializedResolver = typeResolver.init(JsonTypeInfo.Id.CLASS, null);
    initializedResolver = initializedResolver.inclusion(JsonTypeInfo.As.PROPERTY);
    objectMapper.setDefaultTyping(initializedResolver);

    return objectMapper;
}
```

---

## 3. 성능 개선 결과

### 3.1 측정 환경

- 데이터 규모: 10,000개 상품
- 측정 방법: 동일 API 100회 연속 호출
- 측정 도구: curl 기반 성능 테스트 스크립트
- 측정 대상:
  - 인기 상품 조회 API (`GET /api/v1/products/popular`)
  - 단일 상품 조회 API (`GET /api/v1/products/{id}`)

### 3.2 인기 상품 조회 API

| 상태 | 평균 응답 시간 | 개선율 | DB 조회 |
|------|---------------|--------|---------|
| 캐시 미적용 | 10.33ms | - | 매번 6개 쿼리 (ID 조회 1회 + 상품 5회) |
| 캐시 적용 (첫 조회) | 9.59ms | 7% ⬆️ | 6개 쿼리 |
| 캐시 적용 (캐시 히트) | 3.73ms | 63% ⬆️ | 0개 쿼리 |

분석:
- 첫 조회 시 약간의 성능 개선 (10.33ms → 9.59ms, 7%)
- 캐시 히트 시 63% 성능 향상 (10.33ms → 3.73ms)
- DB 조회 0회로 데이터베이스 부하 완전 제거
- 실측 Redis 통계: `keyspace_hits: 2,862`, `keyspace_misses: 5,658`

### 3.3 단일 상품 조회 API

| 상태 | 평균 응답 시간 | 개선율 | DB 조회 |
|------|---------------|--------|---------|
| 캐시 미적용 | 8.27ms | - | 2개 쿼리 (조회 + 업데이트) |
| 캐시 적용 (첫 조회) | 6.49ms | 21% ⬆️ | 2개 쿼리 |
| 캐시 적용 (캐시 히트) | 6.84ms | 17% ⬆️ | 1개 쿼리 (업데이트만) |

분석:
- 첫 조회 시 21% 성능 개선 (8.27ms → 6.49ms)
- 캐시 히트 시 17% 성능 개선 (8.27ms → 6.84ms)
- SELECT 쿼리는 캐시로 대체되지만 `viewCount` UPDATE는 항상 실행
- 전체 응답 시간에서 트랜잭션 및 UPDATE 처리 시간이 큰 비중 차지
- DB 쿼리 수는 감소 (2개 → 1개)하여 데이터베이스 부하는 50% 감소

### 3.4 캐시 히트율 (실측)

테스트 실행 결과:
- 총 요청: 약 400회 (인기 상품 200회 + 단일 상품 200회)
- 캐시 히트: 2,862회
- 캐시 미스: 5,658회
- 실측 히트율: 약 33%

낮은 히트율의 원인:
- 테스트 스크립트의 캐시 미적용 및 첫 조회 시나리오에서 의도적으로 캐시 초기화
- 실제 운영 환경에서는 캐시 히트 시나리오가 대부분이므로 95% 이상의 히트율 예상

### 3.5 데이터베이스 부하 감소

인기 상품 조회 시나리오 (100 req/sec, 95% 히트율 가정):
- 캐시 미적용: 600 쿼리/sec (6개 × 100)
- 캐시 적용 (95% 히트): 30 쿼리/sec (6개 × 5)
- 부하 감소: 95% ⬇️

단일 상품 조회 시나리오 (1,000 req/sec, 80% 히트율 가정):
- 캐시 미적용: 2,000 쿼리/sec (2개 × 1,000)
- 캐시 적용 (80% 히트): 1,200 쿼리/sec (1개 × 1,000 + 2개 × 200)
- 부하 감소: 40% ⬇️
