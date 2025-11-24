# DB를 활용한 동시성 문제 해결방안

## 1. 동시성 문제 식별

- 상품 재고: 여러 사용자가 동시에 동일 상품 주문
- 쿠폰 발급: 한정 수량 쿠폰 동시 발급 요청
- 포인트 사용: 동일 사용자가 동시에 포인트 사용
- 포인트 충전: 동일 사용자가 동시에 포인트 충전

## 2. 재고 관리 동시성 제어

낙관적 락:
- 재고 충돌이 상대적으로 드물게 발생
- 비관적 락보다 성능이 좋음
- 재시도로 충돌 최소화 가능(충돌 자체를 막을 수는 없고, 충돌이 발생하지 않을 때까지 몇 번의 기회를 더 부여함)

## 3. 선착순 쿠폰 발급 동시성 제어

비관적 락:
- 선착순 쿠폰은 동일 쿠폰에 요청이 집중됨
- 정확한 수량 제어가 필수적
- 낙관적 락은 재시도로 인한 불공정성 발생 가능

## 4. 포인트 사용 동시성 제어

낙관적 락:
- 동일 사용자의 동시 주문은 드물게 발생
- 사용자별로 Point가 분리되어 있어 충돌 확률 낮음
- 비관적 락보다 성능이 좋음

## 5. 포인트 충전 동시성 제어

낙관적 락:
- 동일 사용자의 동시 충전은 드물게 발생
- 충전 실패 시 재시도가 자연스러움
- 비관적 락보다 처리량이 높음

## 6. @Transactional 과 @Retryable 함께 사용

### 6.1 @EnableRetry의 기본 order

Spring Retry 2.0.1부터 `@EnableRetry`의 order는 `Ordered.LOWEST_PRECEDENCE - 1`로 변경됐습니다.

변경 히스토리:

1. [spring-projects/spring-retry#22](https://github.com/spring-projects/spring-retry/issues/22) (2015년 1월) - 커스텀 order 지원 요청
2. [spring-projects/spring-retry#333](https://github.com/spring-projects/spring-retry/pull/333) (2023년 2월) - `@EnableRetry`에 order 도입(`order = LOWEST_PRECEDENCE`)
3. [spring-projects/spring-retry#335](https://github.com/spring-projects/spring-retry/pull/335) (2023년 3월) - `order = LOWEST_PRECEDENCE - 1`로 변경

PR #333에서 @xak2000이 아래와 같이 리뷰했습니다.

> "99% of the time you want the order to be @Retry -> @Transactional and not vice versa because many exceptions will be thrown only at the commit phase and could be retried only by starting a new transaction."
>
> (대부분의 경우 @Retry가 @Transactional을 감싸야 합니다. 많은 예외가 커밋 시점에만 발생하고, 새 트랜잭션을 시작해야만 재시도할 수 있기 때문입니다.)

```java
public @interface EnableRetry {
    /**
     * The default is {@code Ordered.LOWEST_PRECEDENCE - 1} in order to make sure the
     * advice is applied before other advices with {@link Ordered#LOWEST_PRECEDENCE} order
     * (e.g. an advice responsible for {@code @Transactional} behavior).
     */
    int order() default Ordered.LOWEST_PRECEDENCE - 1;
}
```

- @Retryable의 order = `Ordered.LOWEST_PRECEDENCE - 1`
- @Transactional의 order = `Ordered.LOWEST_PRECEDENCE`
- order 값이 낮을수록 바깥쪽에서 실행되므로 @Retryable이 먼저 동작

### 6.2 실행 순서

1. @Retryable 진입
2. @Transactional 시작
3. 비즈니스 로직 실행
4. 메서드 종료 -> @Transactional 커밋 -> OptimisticLockException 발생
5. 예외가 @Retryable에게 전파 -> 재시도


