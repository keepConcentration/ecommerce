## E-commerce

## 목차

1. [ERD](#erd)
2. [플로우차트](#플로우차트)
3. [시퀀스 다이어그램](#시퀀스-다이어그램)
4. [동시성 제어 방식 비교](#동시성-제어-방식-비교)
5. [아키텍처 설계 결정사항](#아키텍처-설계-결정사항)

---

## ERD

```mermaid
erDiagram
    USERS ||--o{ CART_ITEMS : ""
    USERS ||--o{ USER_COUPONS : ""
    USERS ||--|| POINTS : ""
    USERS ||--o{ ORDERS : ""
    USERS {
        bigint user_id PK
        datetime created_at
        datetime updated_at
    }

    POINTS {
        bigint point_id PK
        bigint user_id FK
        bigint amount
        datetime created_at
        datetime updated_at
    }

    POINTS ||--o{ POINT_TRANSACTIONS : ""
    POINT_TRANSACTIONS {
        bigint point_transaction_id PK
        bigint point_id FK "INDEX"
        bigint order_id FK "nullable, INDEX"
        bigint amount
        datetime created_at
    }

    ORDERS ||--o{ POINT_TRANSACTIONS : ""
    ORDERS {
        bigint order_id PK
        bigint user_id FK
        bigint total_amount
        bigint discount_amount
        bigint final_amount
        datetime created_at
    }

    PRODUCTS ||--o{ CART_ITEMS : ""
    PRODUCTS ||--o{ ORDER_ITEMS : ""
    PRODUCTS {
        bigint product_id PK
        varchar name
        bigint price
        bigint quantity
        bigint view_count
        bigint sales_count
        datetime created_at
        datetime updated_at
    }

    ORDERS ||--o{ ORDER_ITEMS : ""
    ORDER_ITEMS ||--o| USER_COUPONS : ""
    ORDER_ITEMS {
        bigint order_item_id PK
        bigint order_id FK
        bigint user_id FK
        bigint product_id FK
        bigint user_coupon_id FK "nullable"
        varchar product_name
        bigint quantity
        bigint price
        bigint total_price
        bigint discount_amount
        bigint final_amount
        datetime created_at
    }

    CART_ITEMS {
        bigint cart_item_id PK
        bigint user_id FK
        bigint product_id FK
        bigint quantity
        datetime created_at
        datetime updated_at
    }

    COUPONS ||--o{ USER_COUPONS : ""
    COUPONS {
        bigint coupon_id PK
        varchar name
        bigint discount_amount
        bigint total_quantity
        bigint issued_quantity
        int valid_days "발급 후 유효 일수"
    }

    USER_COUPONS {
        bigint user_coupon_id PK
        bigint user_id FK
        bigint coupon_id FK
        datetime issued_at
        datetime used_at
        datetime expired_at
    }
```

## 플로우차트

### 1. 주문 생성 및 결제 플로우 (CreateOrderUseCase)

```mermaid
flowchart TD
    Start([주문 생성 요청]) --> ValidateCart[장바구니 아이템 조회 및 검증]
    ValidateCart --> CheckCartEmpty{장바구니<br/>비어있음?}
    CheckCartEmpty -->|Yes| ErrorEmptyCart[에러: 주문할 아이템이 없습니다]
    CheckCartEmpty -->|No| ProcessItems[각 아이템별 처리 시작]

    ProcessItems --> DeductStock[재고 차감 및 판매량 증가]
    DeductStock --> StockFail{재고<br/>부족?}
    StockFail -->|Yes| RollbackStockSales[재고 및 판매량 롤백]
    RollbackStockSales --> ErrorStock[에러: 재고 부족]

    StockFail -->|No| CheckCoupon{쿠폰<br/>사용?}
    CheckCoupon -->|Yes| ValidateCoupon[쿠폰 유효성 검증<br/>할인 금액 계산]
    CheckCoupon -->|No| CalcNoDiscount[할인 없이 금액 계산]

    ValidateCoupon --> CouponValid{쿠폰<br/>유효?}
    CouponValid -->|No| RollbackStockSales2[재고 및 판매량 롤백]
    RollbackStockSales2 --> ErrorCoupon[에러: 쿠폰 만료/사용됨]
    CouponValid -->|Yes| AddToOrderData[주문 데이터에 추가]
    CalcNoDiscount --> AddToOrderData

    AddToOrderData --> MoreItems{다음<br/>아이템?}
    MoreItems -->|Yes| ProcessItems
    MoreItems -->|No| CalcFinal[최종 금액 계산]

    CalcFinal --> DeductPoint[포인트 차감]
    DeductPoint --> PointFail{포인트<br/>부족?}
    PointFail -->|Yes| RollbackAll[재고/판매량/포인트 롤백]
    RollbackAll --> ErrorPoint[에러: 포인트 부족]

    PointFail -->|No| CreateOrder[주문 생성]
    CreateOrder --> CreateOrderItems[주문 아이템 생성]
    CreateOrderItems --> MarkCouponUsed{쿠폰<br/>사용?}

    MarkCouponUsed -->|Yes| UpdateCoupon[쿠폰 사용 처리]
    UpdateCoupon --> CreatePointTx[포인트 거래 내역 생성]
    MarkCouponUsed -->|No| CreatePointTx

    CreatePointTx --> ClearCart[주문된 장바구니 아이템 삭제]
    ClearCart --> Success[주문 성공 응답]

    Success --> End([종료])
    ErrorEmptyCart --> End
    ErrorStock --> End
    ErrorCoupon --> End
    ErrorPoint --> End

    style Start fill:#e1f5ff
    style Success fill:#c8e6c9
    style End fill:#e1f5ff
    style ErrorEmptyCart fill:#ffcdd2
    style ErrorStock fill:#ffcdd2
    style ErrorCoupon fill:#ffcdd2
    style ErrorPoint fill:#ffcdd2
    style RollbackStockSales fill:#fff9c4
    style RollbackStockSales2 fill:#fff9c4
    style RollbackAll fill:#fff9c4
```

### 2. 쿠폰 발급 플로우 (IssueCouponUseCase - LockManager 사용)

```mermaid
flowchart TD
    Start([쿠폰 발급 요청]) --> AcquireLock[LockManager로 락 획득<br/>lockKey: coupon:couponId]
    AcquireLock --> LockAcquired{락 획득<br/>성공?}
    LockAcquired -->|No - Timeout| ErrorTimeout[에러: 락 획득 실패]

    LockAcquired -->|Yes| CheckDuplicate[사용자 쿠폰 중복 확인]
    CheckDuplicate --> IsDuplicate{이미<br/>발급받음?}
    IsDuplicate -->|Yes| ReleaseLock1[락 해제]
    ReleaseLock1 --> ErrorDuplicate[에러: 이미 발급받은 쿠폰]

    IsDuplicate -->|No| QueryCoupon[쿠폰 조회]
    QueryCoupon --> CheckExists{쿠폰<br/>존재?}
    CheckExists -->|No| ReleaseLock2[락 해제]
    ReleaseLock2 --> ErrorNotFound[에러: 쿠폰을 찾을 수 없음]

    CheckExists -->|Yes| IssueCoupon[쿠폰 발급 처리<br/>coupon.issue]
    IssueCoupon --> CheckQuantity{남은 수량<br/>> 0?}
    CheckQuantity -->|No| ReleaseLock3[락 해제]
    ReleaseLock3 --> ErrorSoldOut[에러: 쿠폰 소진]

    CheckQuantity -->|Yes| DecreaseQty[발급 수량 증가<br/>✅ Race Condition 방지]
    DecreaseQty --> CalcExpiry[만료일시 계산<br/>발급일 + 유효일수]

    CalcExpiry --> CreateUserCoupon[사용자 쿠폰 생성]
    CreateUserCoupon --> SaveCoupon[쿠폰 상태 저장]

    SaveCoupon --> ReleaseLock4[락 해제]
    ReleaseLock4 --> Success[쿠폰 발급 성공]
    Success --> End([종료])

    ErrorTimeout --> End
    ErrorNotFound --> End
    ErrorDuplicate --> End
    ErrorSoldOut --> End

    style Start fill:#e1f5ff
    style Success fill:#c8e6c9
    style End fill:#e1f5ff
    style ErrorTimeout fill:#ffcdd2
    style ErrorNotFound fill:#ffcdd2
    style ErrorDuplicate fill:#ffcdd2
    style ErrorSoldOut fill:#ffcdd2
    style AcquireLock fill:#fff9c4
    style ReleaseLock1 fill:#fff9c4
    style ReleaseLock2 fill:#fff9c4
    style ReleaseLock3 fill:#fff9c4
    style ReleaseLock4 fill:#fff9c4
    style DecreaseQty fill:#c8e6c9
```

### 3. 장바구니 상품 추가 플로우

```mermaid
flowchart TD
    Start([장바구니 추가 요청]) --> ValidateQty{수량<br/>> 0?}
    ValidateQty -->|No| ErrorInvalidQty[에러: 유효하지 않은 수량]

    ValidateQty -->|Yes| CheckProduct[상품 존재 확인]
    CheckProduct --> ProductExists{상품<br/>존재?}
    ProductExists -->|No| ErrorNotFound[에러: 상품을 찾을 수 없음]

    ProductExists -->|Yes| CheckCartItem[장바구니에 동일 상품 확인]
    CheckCartItem --> ItemExists{이미<br/>존재?}

    ItemExists -->|Yes| UpdateQty[기존 수량에 더하기]
    ItemExists -->|No| CreateItem[새 장바구니 아이템 생성]

    UpdateQty --> Success[장바구니 추가 성공]
    CreateItem --> Success
    Success --> End([종료])

    ErrorInvalidQty --> End
    ErrorNotFound --> End

    style Start fill:#e1f5ff
    style Success fill:#c8e6c9
    style End fill:#e1f5ff
    style ErrorInvalidQty fill:#ffcdd2
    style ErrorNotFound fill:#ffcdd2
```

---

## 시퀀스 다이어그램

### 1. 주문 생성 및 결제 시퀀스 (CreateOrderUseCase)

```mermaid
sequenceDiagram
    actor Client
    participant Controller as OrderController
    participant UseCase as CreateOrderUseCase
    participant Repository as Repositories
    participant PricingService as OrderPricingService

    Client->>Controller: POST /orders<br/>{userId, cartItemCouponMaps}
    activate Controller

    Controller->>UseCase: execute(Input)
    activate UseCase

    Note over UseCase: 1. 장바구니 아이템 조회 및 검증
    loop 각 cartItemId
        UseCase->>Repository: findByIdOrThrow(cartItemId)
        Repository-->>UseCase: CartItem
        UseCase->>UseCase: validateOwnership(userId)
    end

    alt 장바구니가 비어있음
        UseCase-->>Controller: EmptyCartException
        Controller-->>Client: 400 Bad Request
    end

    Note over UseCase: 2. 재고 차감 및 판매량 증가 (Try 블록)
    loop 각 장바구니 아이템
        UseCase->>Repository: findByIdOrThrow(productId)
        Repository-->>UseCase: Product

        UseCase->>UseCase: product.decreaseStock(quantity)
        UseCase->>UseCase: product.increaseSalesCount(quantity)
        UseCase->>Repository: save(product)
        Repository-->>UseCase: savedProduct

        alt 재고 부족
            UseCase->>UseCase: 재고/판매량 롤백
            UseCase-->>Controller: InsufficientStockException
            Controller-->>Client: 409 Conflict
        end

        Note over UseCase: 3. 쿠폰 검증 및 할인 계산
        opt 쿠폰 사용
            UseCase->>Repository: findByIdOrThrow(userCouponId)
            Repository-->>UseCase: UserCoupon
            UseCase->>Repository: findByIdOrThrow(couponId)
            Repository-->>UseCase: Coupon
            UseCase->>UseCase: userCoupon.calculateDiscount(coupon)

            alt 쿠폰 만료 또는 사용됨
                UseCase->>UseCase: 재고/판매량 롤백
                UseCase-->>Controller: CouponException
                Controller-->>Client: 400 Bad Request
            end
        end

        UseCase->>UseCase: orderItemDataList에 추가
    end

    Note over UseCase: 4. 최종 금액 계산
    UseCase->>PricingService: calculateItemTotal(product, quantity)
    PricingService-->>UseCase: itemTotal
    UseCase->>PricingService: calculateFinalAmount(total, discount)
    PricingService-->>UseCase: finalAmount

    Note over UseCase: 5. 포인트 차감
    UseCase->>Repository: findByUserIdOrThrow(userId)
    Repository-->>UseCase: Point
    UseCase->>UseCase: point.deduct(finalAmount)
    UseCase->>Repository: save(point)
    Repository-->>UseCase: savedPoint

    alt 포인트 부족
        UseCase->>UseCase: 재고/판매량/포인트 롤백
        UseCase-->>Controller: InsufficientPointsException
        Controller-->>Client: 409 Conflict
    end

    Note over UseCase: 6. 주문 생성
    UseCase->>UseCase: Order.create(userId, totalAmount, discountAmount)
    UseCase->>Repository: save(order)
    Repository-->>UseCase: savedOrder

    Note over UseCase: 7. 주문 아이템 생성 및 쿠폰 사용 처리
    loop 각 orderItemData
        UseCase->>UseCase: OrderItem.create(...)
        UseCase->>Repository: save(orderItem)
        Repository-->>UseCase: savedOrderItem

        opt 쿠폰 사용
            UseCase->>UseCase: userCoupon.use()
            UseCase->>Repository: save(userCoupon)
            Repository-->>UseCase: savedUserCoupon
        end
    end

    Note over UseCase: 8. 포인트 거래 내역 생성
    UseCase->>UseCase: PointTransaction.createDeduction(...)
    UseCase->>Repository: save(pointTransaction)
    Repository-->>UseCase: savedPointTransaction

    Note over UseCase: 9. 주문된 장바구니 아이템 삭제
    loop 각 cartItem
        UseCase->>Repository: deleteById(cartItemId)
    end

    UseCase-->>Controller: Output(orderResponse)
    deactivate UseCase

    Controller-->>Client: 201 Created<br/>{orderId, orderItems, finalAmount, ...}
    deactivate Controller

    Note over UseCase: Catch 블록: 오류 발생 시 자동 롤백<br/>- 재고/판매량 복구<br/>- 포인트 복구<br/>- 쿠폰 사용 취소
```

### 2. 쿠폰 발급 시퀀스 (IssueCouponUseCase - LockManager 사용)

```mermaid
sequenceDiagram
    actor Client
    participant Controller as CouponController
    participant UseCase as IssueCouponUseCase
    participant LockManager as LockManager
    participant Repository as Repositories

    Client->>Controller: POST /coupons/{couponId}/issue<br/>{userId}
    activate Controller

    Controller->>UseCase: execute(Input)
    activate UseCase

    Note over UseCase: lockKey = "coupon:" + couponId

    UseCase->>LockManager: executeWithLock(lockKey, lambda)
    activate LockManager
    LockManager->>LockManager: 락 획득 시도 (10초 타임아웃)

    alt 락 획득 실패
        LockManager-->>UseCase: LockAcquisitionException
        UseCase-->>Controller: Exception
        Controller-->>Client: 500 Internal Server Error
    end

    Note over LockManager: ✅ 락 획득 성공 - 임계 영역 진입

    Note over UseCase: 1. 중복 발급 확인
    LockManager->>Repository: existsByUserIdAndCouponId(userId, couponId)
    Repository-->>LockManager: boolean

    alt 이미 발급받음
        LockManager->>LockManager: 락 해제
        LockManager-->>UseCase: CouponAlreadyIssuedException
        UseCase-->>Controller: Exception
        Controller-->>Client: 409 Conflict
    end

    Note over UseCase: 2. 쿠폰 조회
    LockManager->>Repository: findByIdOrThrow(couponId)
    Repository-->>LockManager: Coupon

    alt 쿠폰이 존재하지 않음
        LockManager->>LockManager: 락 해제
        LockManager-->>UseCase: CouponNotFoundException
        UseCase-->>Controller: Exception
        Controller-->>Client: 404 Not Found
    end

    Note over UseCase: 3. 쿠폰 발급 처리
    LockManager->>LockManager: coupon.issue()
    Note over LockManager: 수량 검증 및 발급 수량 증가

    alt 쿠폰 소진
        LockManager->>LockManager: 락 해제
        LockManager-->>UseCase: CouponSoldOutException
        UseCase-->>Controller: Exception
        Controller-->>Client: 409 Conflict
    end

    Note over UseCase: 4. 만료일시 계산 및 사용자 쿠폰 생성
    LockManager->>LockManager: UserCoupon.issue(userId, couponId, validDays)
    LockManager->>Repository: save(userCoupon)
    Repository-->>LockManager: savedUserCoupon

    Note over UseCase: 5. 쿠폰 상태 저장
    LockManager->>Repository: save(coupon)
    Repository-->>LockManager: savedCoupon

    LockManager->>LockManager: 락 해제
    Note over LockManager: ✅ 임계 영역 종료

    LockManager-->>UseCase: Output(userCouponResponse)
    deactivate LockManager

    UseCase-->>Controller: Output
    deactivate UseCase

    Controller-->>Client: 201 Created<br/>{userCouponId, couponName, expiredAt, ...}
    deactivate Controller

    Note over LockManager: Race Condition 방지<br/>동일 쿠폰에 대한 동시 발급 요청을<br/>순차적으로 처리
```

### 3. 즉시 구매 시퀀스 (CreateDirectOrderUseCase - 장바구니 없이)

```mermaid
sequenceDiagram
    actor Client
    participant Controller as OrderController
    participant UseCase as CreateDirectOrderUseCase
    participant Repository as Repositories
    participant PricingService as OrderPricingService

    Client->>Controller: POST /orders/direct<br/>{userId, productId, quantity, userCouponId}
    activate Controller

    Controller->>UseCase: execute(Input)
    activate UseCase

    Note over UseCase: Try 블록 시작

    Note over UseCase: 1. 상품 조회, 재고 차감 및 판매량 증가
    UseCase->>Repository: findByIdOrThrow(productId)
    Repository-->>UseCase: Product

    UseCase->>UseCase: product.decreaseStock(quantity)
    UseCase->>UseCase: product.increaseSalesCount(quantity)
    UseCase->>Repository: save(product)
    Repository-->>UseCase: savedProduct

    alt 상품 없음 또는 재고 부족
        UseCase-->>Controller: ProductNotFoundException or InsufficientStockException
        Controller-->>Client: 404 Not Found or 409 Conflict
    end

    Note over UseCase: 2. 쿠폰 검증 및 할인 계산
    opt 쿠폰 사용
        UseCase->>Repository: findByIdOrThrow(userCouponId)
        Repository-->>UseCase: UserCoupon
        UseCase->>Repository: findByIdOrThrow(couponId)
        Repository-->>UseCase: Coupon
        UseCase->>UseCase: userCoupon.calculateDiscount(coupon)

        alt 쿠폰 만료 또는 사용됨
            UseCase->>UseCase: 재고/판매량 롤백
            UseCase-->>Controller: CouponException
            Controller-->>Client: 400 Bad Request
        end
    end

    Note over UseCase: 3. 금액 계산
    UseCase->>PricingService: calculateItemTotal(product, quantity)
    PricingService-->>UseCase: totalAmount
    UseCase->>PricingService: calculateFinalAmount(totalAmount, discountAmount)
    PricingService-->>UseCase: finalAmount

    Note over UseCase: 4. 포인트 차감
    UseCase->>Repository: findByUserIdOrThrow(userId)
    Repository-->>UseCase: Point
    UseCase->>UseCase: point.deduct(finalAmount)
    UseCase->>Repository: save(point)
    Repository-->>UseCase: savedPoint

    alt 포인트 부족
        UseCase->>UseCase: 재고/판매량/포인트 롤백
        UseCase-->>Controller: InsufficientPointsException
        Controller-->>Client: 409 Conflict
    end

    Note over UseCase: 5. 주문 생성
    UseCase->>UseCase: Order.create(userId, totalAmount, discountAmount)
    UseCase->>Repository: save(order)
    Repository-->>UseCase: savedOrder

    Note over UseCase: 6. 주문 아이템 생성
    UseCase->>UseCase: OrderItem.create(...)
    UseCase->>Repository: save(orderItem)
    Repository-->>UseCase: savedOrderItem

    Note over UseCase: 7. 쿠폰 사용 처리
    opt 쿠폰 사용
        UseCase->>UseCase: userCoupon.use()
        UseCase->>Repository: save(userCoupon)
        Repository-->>UseCase: savedUserCoupon
    end

    Note over UseCase: 8. 포인트 거래 내역 생성
    UseCase->>UseCase: PointTransaction.createDeduction(...)
    UseCase->>Repository: save(pointTransaction)
    Repository-->>UseCase: savedPointTransaction

    UseCase-->>Controller: Output(orderResponse)
    deactivate UseCase

    Controller-->>Client: 201 Created<br/>{orderId, orderItem, finalAmount, ...}
    deactivate Controller

    Note over UseCase: Catch 블록: 오류 발생 시 자동 롤백<br/>- 재고/판매량 복구<br/>- 포인트 복구<br/>- 쿠폰 사용 취소
```

---

### 동시성 제어 방식 비교

#### 1. synchronized

- 구현: `synchronized` 키워드 사용
- 적용 범위: 코드 블록, 메서드 단위
- 장점:
  - 자바에서 기본으로 제공하는 기능
  - 락 해제/예외 처리 자동 보장
  - 코드 가독성 높음
- 단점:
  - 블록 범위가 넓어지면 성능 저하 (모니터 락 경쟁)
  - 타임아웃 등 세밀한 제어 불가
  - 분산 환경에서는 동기화 불가

#### 1.1. 임계 영역

- 한 번에 하나의 쓰레드만 접근을 허용해야 하는 코드 영역
```java
class Counter {
    private int count = 0;

    public void increment() {
        // 임계 영역 시작
        count++;
        // 임계 영역 끝
    }
}
```

#### 1.2. 모니터

  - 임계 영역을 관리하는 객체
  - 모든 자바 객체는 모니터를 가지고 있음.
  - 모니터는 내부적으로 하나의 락(lock) 메커니즘을 가지고 있음.
```java
synchronized void increment() {
    count++; // 이 메서드는 모니터(객체)에 의해 보호됨
}
```

#### 1.3. 모니터 락

  - 모니터 내부의 락
  - 스레드가 임계영역에 진입하기 위해 필요한 락 객체
  - 자바에서 어떤 객체든 `synchronized` 키워드를 이용해 모니터 락을 획득할 수 있음.
```java
synchronized (this) { // this 객체의 모니터 락 획득
    count++;
} // 블록이 끝나면 락 자동 해제
```


#### 2. ReentrantLock

- 구현: `ReentrantLock` + `ConcurrentHashMap`
- 적용 범위: 단일 JVM
- 장점:
  - 구현이 간단함
  - DB에 부하가 없음
  - synchronized에 비해 빠른 성능
  - 타임아웃, 공정성 설정 가능
- 단점:
  - 분산 환경에서 지원하지 않음.
  - 반드시 수동으로 unlock() 호출 필요(메모리 누수 위험).
  - 서버 재시작 시 락 상태 초기화됨

#### 2.1. 공정성

- 스레드가 락을 획득하는 순서에 대한 설정

```java
ReentrantLock nonFairLock = new ReentrantLock(); // 기본값: 비공정 락
ReentrantLock fairLock = new ReentrantLock(true); // 공정락
```

#### 2.1.1. 공정락

- 대기 큐(FIFO)에 들어온 순서대로 락을 획득함
- 대기 순서 보장 덕분에 기아 현상이 거의 없음.
- 비공정 락보다 성능이 떨어짐

#### 2.1.2. 비공정락

- 락 획득 시점에 대기 큐를 무시함
- 락이 해제되는 순간 대기 중인 스레드보다 나중에 온 스레드가 먼저 락을 획득할 수도 있음.
- 대기 순서가 보장되지 않아 기아 현상이 생길 수 있음.
- 공정 락보다 성능이 좋음

#### 3. ReentrantReadWriteLock

- 구현: `ReentrantReadWriteLock`
- 적용 범위: 단일 JVM
- 장점:
  - 읽기/쓰기 작업을 분리하여 여러 스레드가 동시에 읽기 가능
  - DB에 부하가 없음
  - synchronized에 비해 빠른 성능
  - 타임아웃, 공정성 설정 가능
  - 읽기가 많은 환경에서는 성능이 좋음
- 단점:
  - 락 관리가 까다로움
  - 읽기 스레드가 많을 경우 쓰기 스레드가 기아 상태에 빠질 수 있음.
  - 분산 환경에서 지원하지 않음.
  - 반드시 수동으로 unlock() 호출 필요(메모리 누수 위험).
  - 서버 재시작 시 락 상태 초기화됨

#### 3.1. 읽기락

- 여러 스레드가 동시에 읽기 가능

```java
ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
private int value = 0;

public int read() {
  lock.readLock().lock();
  try {
    return value;
  } finally {
    lock.readLock().unlock();
  }
}
```

#### 3.2. 쓰기락

- 단 하나의 스레드만 쓰기 가능
```java
ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
private int value = 0;

public void write(int newValue) {
  lock.writeLock().lock();
  try {
    value = newValue;
  } finally {
    lock.writeLock().unlock();
  }
}
```

#### 3.3. 읽기락과 쓰기락 관계

- 읽기 중에는 쓰기 금지, 쓰기 중에는 읽기 금지
- 읽기 락 → 쓰기 락 업그레이드 불가
- 쓰기 락 → 읽기 락 다운그레이드는 가능

#### 4. 분산 락

- 구현: Redis, ZooKeeper, etcd 등 외부 시스템
- 적용 범위: 분산 환경
- 장점:
  - 여러 JVM 간 동기화 가능
  - 분산 환경에서 데이터 정합성 유지
  - 
- 단점:
  - 외부 시스템 의존 (Redis 등)
  - 네트워크 지연 시 성능 저하

---

### 선택 기준 및 트레이드오프

#### 현재 선택: ReentrantLock

**선택 이유:**

- 현재 `ConcurrentHashMap`을 이용한 인메모리 리포지토리 사용
- DB가 없어 DB 락 불가능
- 단일 애플리케이션
- 메모리 내 락으로 성능이 우수함

**트레이드오프:**

- 이득
  - 빠른 개발 속도
  - 단순한 구조
  - 높은 성능
- 손실
  - 단일 JVM 제약
  - 확장성 제한
  - 메모리 누수 위험

---

## 아키텍처 설계 결정사항

#### 설계 결정 이유

1. UseCase 

- 장점:
  - 각 유스케이스의 명확한 트랜잭션 경계
  - 비즈니스 요구사항과 1:1 매핑
  - 테스트 격리 용이
  - 새로운 기능 추가 시 기존 코드 영향 없음

- 단점:
  - 클래스 수 증가 (현재 20+ UseCase)
  - 공통 로직 중복 가능성(장바구니 주문과 상품 직접 주문) → Domain Service로 해결

2. Rich Domain Model

- 장점:
  - 비즈니스 규칙 캡슐화
  - 재사용성
  - 테스트 용이

3. Factory Method 패턴

- 이유:
  - ID 할당 책임을 Repository가 담당함

4. 수동 롤백 방식 (Try-Catch)

- 현재 (인메모리 저장소):
  - `@Transactional` 사용 불가

5. 도메인 모듈별 ErrorCode enum 적용 및 BaseException 상속

- 장점:
  - HTTP 상태 코드가 도메인 예외에 명시됨
  - 에러 코드 중앙 관리

- 단점:
  - 런타임 예외 위험

6. ApiResponse 래퍼 패턴

- 장점:
  - 일관된 응답 구조
  - 에러 처리 일관성 (모든 API 동일 구조)
  - 문서화 용이

- 대안
  - HATEOAS

## 인기 상품 조회 쿼리 성능 분석

### 1. 개요

인기 상품 조회 쿼리 내에 정렬 시 성능 저하가 발생할 것으로 예상합니다.

### 2. 문제 쿼리

```sql
SELECT * 
FROM products
ORDER BY (view_count * 0.1 + sales_count * 0.9) DESC
LIMIT 5;
```

### 3. 실행 계획 (인덱스 없음)

#### EXPLAIN 결과

![img_2.png](img_2.png)

| id | type | table    | possible_keys | key  | rows | Extra          |
| -- | ---- |----------| ------------- | ---- |------| -------------- |
| 1  | ALL  | products | NULL          | NULL | 9840 | Using filesort |

#### EXPLAIN ANALYZE 결과

![img_3.png](img_3.png)

```
-> Sort: ((products.view_count * 0.1) + (products.sales_count * 0.9)) DESC (cost=1024 rows=9840) (actual time=24.6..26.1 rows=10000 loops=1)
    -> Table scan on products  (cost=1024 rows=9840) (actual time=0.862..10.5 rows=10000 loops=1)
```

#### 분석 요약

- Full Table Scan 발생: 모든 행(약 10,000개)을 읽음
- Filesort 사용: 메모리 또는 디스크 정렬 수행
- 정렬식 계산으로 인해 인덱스 활용 불가
- 평균 실행 시간
  - 10,000건: 약 50ms
  -100,000건: 약 500ms

#### 병목 원인

- 계산식 기반 정렬로 인해 인덱스 사용 불가
- 모든 행을 읽고 계산 및 정렬하는 비효율적 수행

### 4. 개선 방안
4.1 popularity_score 컬럼 추가 및 인덱스 생성

조회 시 매번 (view_count * 0.1 + sales_count * 0.9)를 계산하지 않도록
별도의 컬럼을 추가하고 인덱싱합니다.

```
ALTER TABLE products 
ADD COLUMN popularity_score DECIMAL(10,2) 
GENERATED ALWAYS AS (view_count * 0.1 + sales_count * 0.9) STORED;

CREATE INDEX idx_popularity_score ON products (popularity_score DESC);
```

### 5. 실행 계획 (인덱스 추가 후)

#### EXPLAIN 결과

![img_4.png](img_4.png)

| id   | type | table    | possible_keys | key | rows | Extra |
|------|------|----------|---------------|-----|------|-------|
| 1    |      | products |               |     |      |       |

#### EXPLAIN ANALYZE 결과

![img_5.png](img_5.png)

```
'-> Limit: 5 row(s)  (cost=0.0207 rows=5) (actual time=0.239..0.241 rows=5 loops=1)
    -> Index scan on p using idx_popularity_score  (cost=0.0207 rows=5) (actual time=0.234..0.235 rows=5 loops=1)

```

### 6. 성능 비교

#### 6.1 실행 계획 비교

| 항목        | 인덱스 없음 (Before)       | 인덱스 있음 (After)       | 개선 효과          |
|-----------|-----------------------|----------------------|----------------|
| **type**  | ALL (Full Table Scan) | index (Index Scan)   | ✅ Full Scan 제거 |
| **key**   | NULL                  | idx_popularity_score | ✅ 인덱스 사용       |
| **rows**  | 9,840                 | 5                    | ✅ 99.95% 감소    |
| **Extra** | Using filesort        | -                    | ✅ 정렬 작업 제거     |

#### 6.2 실제 실행 시간 비교

| 데이터 건수    | Before (Filesort) | After (Index Scan) | 성능 개선 비율     |
|-----------|-------------------|--------------------|--------------|
| 10,000건   | ~50ms             | ~0.24ms            | **약 208배**   |
| 100,000건  | ~500ms            | ~0.24ms            | **약 2,083배** |

#### 6.3 핵심 개선 사항

1. **Full Table Scan → Index Scan**
   - Before: 10,000개 행 전체를 읽고 정렬
   - After: 인덱스에서 상위 5개만 직접 조회

2. **Filesort 제거**
   - Before: 메모리/디스크에서 정렬 작업 수행
   - After: 인덱스가 이미 DESC 순서로 정렬되어 있어 정렬 불필요

3. **계산식 제거**
   - Before: 각 행마다 `(view_count * 0.1 + sales_count * 0.9)` 계산
   - After: 사전 계산된 `popularity_score` 컬럼 사용

### 7. 결론

#### 7.1 최적화 효과

인기 상품 조회 쿼리에 `popularity_score` 컬럼 추가 및 인덱스 생성을 통해:

- **성능 개선: 208~2,083배**
- **실행 시간: 50ms → 0.24ms (10,000건 기준)**
- **Full Table Scan 완전 제거**

#### 7.2 핵심 학습 포인트

1. **계산식 기반 정렬은 인덱스를 사용할 수 없음**
   - `ORDER BY (view_count * 0.1 + sales_count * 0.9)`는 항상 Filesort 발생
   - 해결책: 계산 결과를 컬럼으로 저장 (Computed Column 패턴)

2. **LIMIT 쿼리에서 인덱스의 중요성**
   - 상위 N개만 필요한 경우, 인덱스가 있으면 N개만 읽고 종료
   - 인덱스가 없으면 전체 데이터를 읽고 정렬 후 N개 선택

3. **DESC 인덱스 최적화**
   - `ORDER BY ... DESC` 쿼리는 DESC 인덱스로 Backward Index Scan 수행
   - MySQL 8.0+에서는 인덱스 생성 시 DESC 키워드 지원

#### 7.4 트레이드오프

**이득:**
- 조회 성능 208~2,083배 개선
- Filesort로 인한 메모리/디스크 I/O 부담 제거

**비용:**
- 추가 컬럼으로 인한 디스크 사용량 증가 (컬럼당 8 bytes)
- `view_count`, `sales_count` 업데이트 시 `popularity_score`도 함께 업데이트 필요
- 인덱스 유지 비용 (INSERT/UPDATE 시 인덱스 재정렬)

**결론:** 조회 빈도가 높고 데이터가 많은 시스템에서는 이득이 비용을 압도적으로 상회합니다.

