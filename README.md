## E-commerce

### ERD

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
    participant CartRepo as CartItemRepository
    participant ProductRepo as ProductRepository
    participant UserCouponRepo as UserCouponRepository
    participant CouponRepo as CouponRepository
    participant PricingService as OrderPricingService
    participant PointRepo as PointRepository
    participant OrderRepo as OrderRepository
    participant OrderItemRepo as OrderItemRepository
    participant PointTxRepo as PointTransactionRepository

    Client->>Controller: POST /orders<br/>{userId, cartItemCouponMaps}
    activate Controller

    Controller->>UseCase: execute(Input)
    activate UseCase

    Note over UseCase: 1. 장바구니 아이템 조회 및 검증
    loop 각 cartItemId
        UseCase->>CartRepo: findByIdOrThrow(cartItemId)
        CartRepo-->>UseCase: CartItem
        UseCase->>UseCase: validateOwnership(userId)
    end

    alt 장바구니가 비어있음
        UseCase-->>Controller: IllegalStateException
        Controller-->>Client: 400 Bad Request
    end

    Note over UseCase: 2. 재고 차감 및 판매량 증가 (Try 블록)
    loop 각 장바구니 아이템
        UseCase->>ProductRepo: findByIdOrThrow(productId)
        ProductRepo-->>UseCase: Product

        UseCase->>UseCase: product.decreaseStock(quantity)
        UseCase->>UseCase: product.increaseSalesCount(quantity)
        UseCase->>ProductRepo: save(product)
        ProductRepo-->>UseCase: savedProduct

        alt 재고 부족
            UseCase->>UseCase: 재고/판매량 롤백
            UseCase-->>Controller: InsufficientStockException
            Controller-->>Client: 409 Conflict
        end

        Note over UseCase: 3. 쿠폰 검증 및 할인 계산
        opt 쿠폰 사용
            UseCase->>UserCouponRepo: findByIdOrThrow(userCouponId)
            UserCouponRepo-->>UseCase: UserCoupon
            UseCase->>CouponRepo: findByIdOrThrow(couponId)
            CouponRepo-->>UseCase: Coupon
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
    UseCase->>PointRepo: findByUserIdOrThrow(userId)
    PointRepo-->>UseCase: Point
    UseCase->>UseCase: point.deduct(finalAmount)
    UseCase->>PointRepo: save(point)
    PointRepo-->>UseCase: savedPoint

    alt 포인트 부족
        UseCase->>UseCase: 재고/판매량/포인트 롤백
        UseCase-->>Controller: InsufficientPointsException
        Controller-->>Client: 409 Conflict
    end

    Note over UseCase: 6. 주문 생성
    UseCase->>UseCase: Order.create(userId, totalAmount, discountAmount)
    UseCase->>OrderRepo: save(order)
    OrderRepo-->>UseCase: savedOrder

    Note over UseCase: 7. 주문 아이템 생성 및 쿠폰 사용 처리
    loop 각 orderItemData
        UseCase->>UseCase: OrderItem.create(...)
        UseCase->>OrderItemRepo: save(orderItem)
        OrderItemRepo-->>UseCase: savedOrderItem

        opt 쿠폰 사용
            UseCase->>UseCase: userCoupon.use()
            UseCase->>UserCouponRepo: save(userCoupon)
            UserCouponRepo-->>UseCase: savedUserCoupon
        end
    end

    Note over UseCase: 8. 포인트 거래 내역 생성
    UseCase->>UseCase: PointTransaction.createDeduction(...)
    UseCase->>PointTxRepo: save(pointTransaction)
    PointTxRepo-->>UseCase: savedPointTransaction

    Note over UseCase: 9. 주문된 장바구니 아이템 삭제
    loop 각 cartItem
        UseCase->>CartRepo: deleteById(cartItemId)
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
    participant UserCouponRepo as UserCouponRepository
    participant CouponRepo as CouponRepository

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
    LockManager->>UserCouponRepo: existsByUserIdAndCouponId(userId, couponId)
    UserCouponRepo-->>LockManager: boolean

    alt 이미 발급받음
        LockManager->>LockManager: 락 해제
        LockManager-->>UseCase: CouponAlreadyIssuedException
        UseCase-->>Controller: Exception
        Controller-->>Client: 409 Conflict
    end

    Note over UseCase: 2. 쿠폰 조회
    LockManager->>CouponRepo: findByIdOrThrow(couponId)
    CouponRepo-->>LockManager: Coupon

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
    LockManager->>UserCouponRepo: save(userCoupon)
    UserCouponRepo-->>LockManager: savedUserCoupon

    Note over UseCase: 5. 쿠폰 상태 저장
    LockManager->>CouponRepo: save(coupon)
    CouponRepo-->>LockManager: savedCoupon

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
    participant ProductRepo as ProductRepository
    participant UserCouponRepo as UserCouponRepository
    participant CouponRepo as CouponRepository
    participant PricingService as OrderPricingService
    participant PointRepo as PointRepository
    participant OrderRepo as OrderRepository
    participant OrderItemRepo as OrderItemRepository
    participant PointTxRepo as PointTransactionRepository

    Client->>Controller: POST /orders/direct<br/>{userId, productId, quantity, userCouponId}
    activate Controller

    Controller->>UseCase: execute(Input)
    activate UseCase

    Note over UseCase: Try 블록 시작

    Note over UseCase: 1. 상품 조회, 재고 차감 및 판매량 증가
    UseCase->>ProductRepo: findByIdOrThrow(productId)
    ProductRepo-->>UseCase: Product

    UseCase->>UseCase: product.decreaseStock(quantity)
    UseCase->>UseCase: product.increaseSalesCount(quantity)
    UseCase->>ProductRepo: save(product)
    ProductRepo-->>UseCase: savedProduct

    alt 상품 없음 또는 재고 부족
        UseCase-->>Controller: ProductNotFoundException or InsufficientStockException
        Controller-->>Client: 404 Not Found or 409 Conflict
    end

    Note over UseCase: 2. 쿠폰 검증 및 할인 계산
    opt 쿠폰 사용
        UseCase->>UserCouponRepo: findByIdOrThrow(userCouponId)
        UserCouponRepo-->>UseCase: UserCoupon
        UseCase->>CouponRepo: findByIdOrThrow(couponId)
        CouponRepo-->>UseCase: Coupon
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
    UseCase->>PointRepo: findByUserIdOrThrow(userId)
    PointRepo-->>UseCase: Point
    UseCase->>UseCase: point.deduct(finalAmount)
    UseCase->>PointRepo: save(point)
    PointRepo-->>UseCase: savedPoint

    alt 포인트 부족
        UseCase->>UseCase: 재고/판매량/포인트 롤백
        UseCase-->>Controller: InsufficientPointsException
        Controller-->>Client: 409 Conflict
    end

    Note over UseCase: 5. 주문 생성
    UseCase->>UseCase: Order.create(userId, totalAmount, discountAmount)
    UseCase->>OrderRepo: save(order)
    OrderRepo-->>UseCase: savedOrder

    Note over UseCase: 6. 주문 아이템 생성
    UseCase->>UseCase: OrderItem.create(...)
    UseCase->>OrderItemRepo: save(orderItem)
    OrderItemRepo-->>UseCase: savedOrderItem

    Note over UseCase: 7. 쿠폰 사용 처리
    opt 쿠폰 사용
        UseCase->>UseCase: userCoupon.use()
        UseCase->>UserCouponRepo: save(userCoupon)
        UserCouponRepo-->>UseCase: savedUserCoupon
    end

    Note over UseCase: 8. 포인트 거래 내역 생성
    UseCase->>UseCase: PointTransaction.createDeduction(...)
    UseCase->>PointTxRepo: save(pointTransaction)
    PointTxRepo-->>UseCase: savedPointTransaction

    UseCase-->>Controller: Output(orderResponse)
    deactivate UseCase

    Controller-->>Client: 201 Created<br/>{orderId, orderItem, finalAmount, ...}
    deactivate Controller

    Note over UseCase: Catch 블록: 오류 발생 시 자동 롤백<br/>- 재고/판매량 복구<br/>- 포인트 복구<br/>- 쿠폰 사용 취소
```
