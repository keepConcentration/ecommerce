## E-commerce

### ERD

```mermaid
erDiagram
    USERS ||--o{ CART_ITEMS : ""
    USERS ||--o{ USER_COUPONS : ""
    USERS ||--|| POINTS : ""
    USERS ||--o{ ORDER_ITEMS : ""
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
        bigint point_transaction_id
        bigint point_id "INDEX"
        bigint amount
        datetime created_at
    }

    PRODUCTS ||--o{ CART_ITEMS : ""
    PRODUCTS ||--o{ ORDER_ITEMS : ""
    PRODUCTS {
        bigint product_id PK
        varchar name
        bigint price
        bigint quantity
        datetime created_at
        datetime updated_at
    }

    ORDER_ITEMS ||--o| CART_ITEMS : ""
    ORDER_ITEMS ||--o| USER_COUPONS : ""
    ORDER_ITEMS {
        bigint order_item_id PK
        bigint user_id FK
        bigint product_id FK
        bigint cart_item_id FK
        bigint user_coupon_id FK
        bigint point_transaction_id FK
        varchar product_name
        bigint quantity
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

### 1. 주문 생성 및 결제 플로우

```mermaid
flowchart TD
    Start([주문 생성 요청]) --> GetCart[장바구니 조회]
    GetCart --> CheckCartEmpty{장바구니<br/>비어있음?}
    CheckCartEmpty -->|Yes| ErrorEmptyCart[에러: 장바구니가 비어있습니다]
    CheckCartEmpty -->|No| CheckStock[모든 상품 재고 확인]

    CheckStock --> StockSufficient{재고<br/>충분?}
    StockSufficient -->|No| ErrorStock[에러: 재고 부족]
    StockSufficient -->|Yes| DeductStock[재고 차감]

    DeductStock --> CheckCoupon{쿠폰<br/>사용?}
    CheckCoupon -->|No| CalcNoDiscount[할인 없이 금액 계산]
    CheckCoupon -->|Yes| ValidateCoupon[쿠폰 유효성 검증]

    ValidateCoupon --> CouponValid{쿠폰<br/>유효?}
    CouponValid -->|No| ErrorCoupon[에러: 쿠폰 만료/사용됨]
    CouponValid -->|Yes| ApplyCoupon[쿠폰 할인 적용]

    ApplyCoupon --> CalcFinal[최종 금액 계산]
    CalcNoDiscount --> CalcFinal

    CalcFinal --> CheckPoint{포인트<br/>충분?}
    CheckPoint -->|No| RollbackStock[재고 롤백]
    RollbackStock --> ErrorPoint[에러: 포인트 부족]

    CheckPoint -->|Yes| DeductPoint[포인트 차감]
    DeductPoint --> CreateOrder[주문 생성]
    CreateOrder --> MarkCouponUsed{쿠폰<br/>사용?}

    MarkCouponUsed -->|Yes| UpdateCoupon[쿠폰 사용 처리]
    MarkCouponUsed -->|No| ClearCart[장바구니 비우기]
    UpdateCoupon --> ClearCart

    ClearCart --> SendExternal[외부 시스템 데이터 전송]
    SendExternal --> ExternalSuccess{전송<br/>성공?}
    ExternalSuccess -->|No| LogFailure[전송 실패 로그 기록]
    ExternalSuccess -->|Yes| Success
    LogFailure --> Success[주문 성공 응답]

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
```

### 2. 쿠폰 발급 플로우 (동시성 제어 없음)

```mermaid
flowchart TD
    Start([쿠폰 발급 요청]) --> BeginTx[트랜잭션 시작]
    BeginTx --> QueryCoupon[쿠폰 조회<br/>SELECT]

    QueryCoupon --> CheckExists{쿠폰<br/>존재?}
    CheckExists -->|No| ErrorNotFound[에러: 쿠폰을 찾을 수 없음]

    CheckExists -->|Yes| CheckDuplicate[사용자 쿠폰 중복 확인]
    CheckDuplicate --> IsDuplicate{이미<br/>발급받음?}
    IsDuplicate -->|Yes| ErrorDuplicate[에러: 이미 발급받은 쿠폰]

    IsDuplicate -->|No| CheckQuantity{남은 수량<br/>> 0?}
    CheckQuantity -->|No| ErrorSoldOut[에러: 쿠폰 소진]

    CheckQuantity -->|Yes| DecreaseQty[수량 감소<br/>total_quantity - 1<br/>⚠️ Race Condition 가능]
    DecreaseQty --> CalcExpiry[만료일시 계산<br/>발급일 + 유효일수]

    CalcExpiry --> CreateUserCoupon[사용자 쿠폰 생성]
    CreateUserCoupon --> CommitTx[트랜잭션 커밋]

    CommitTx --> Success[쿠폰 발급 성공]
    Success --> End([종료])

    ErrorNotFound --> RollbackTx[트랜잭션 롤백]
    ErrorDuplicate --> RollbackTx
    ErrorSoldOut --> RollbackTx
    RollbackTx --> End

    style Start fill:#e1f5ff
    style Success fill:#c8e6c9
    style End fill:#e1f5ff
    style ErrorNotFound fill:#ffcdd2
    style ErrorDuplicate fill:#ffcdd2
    style ErrorSoldOut fill:#ffcdd2
    style DecreaseQty fill:#ffe0b2
    style BeginTx fill:#fff9c4
    style CommitTx fill:#fff9c4
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

### 1. 주문 생성 및 결제 시퀀스

```mermaid
sequenceDiagram
    actor Client
    participant Controller as OrderController
    participant Service as OrderService
    participant CartRepo as CartRepository
    participant ProductRepo as ProductRepository
    participant CouponRepo as CouponRepository
    participant PointRepo as PointRepository
    participant OrderRepo as OrderRepository
    participant ExternalAPI as 외부 시스템
    participant DB as Database

    Client->>Controller: POST /orders<br/>{userId, cartItemCouponMaps}
    activate Controller

    Controller->>Service: createOrder(request)
    activate Service

    Note over Service: 1. 장바구니 조회
    Service->>CartRepo: findByUserId(userId)
    activate CartRepo
    CartRepo->>DB: SELECT * FROM cart_items<br/>WHERE user_id = ?
    DB-->>CartRepo: cart_items
    CartRepo-->>Service: List<CartItem>
    deactivate CartRepo

    alt 장바구니가 비어있음
        Service-->>Controller: Exception: 장바구니가 비어있습니다
        Controller-->>Client: 400 Bad Request
    end

    Note over Service: 2. 재고 확인 및 차감
    loop 각 장바구니 아이템
        Service->>ProductRepo: findByIdWithLock(productId)
        activate ProductRepo
        ProductRepo->>DB: SELECT * FROM products<br/>WHERE product_id = ?<br/>FOR UPDATE
        DB-->>ProductRepo: product
        ProductRepo-->>Service: Product
        deactivate ProductRepo

        alt 재고 부족
            Service-->>Controller: Exception: 재고 부족
            Controller-->>Client: 409 Conflict
        end

        Service->>ProductRepo: decreaseStock(productId, quantity)
        activate ProductRepo
        ProductRepo->>DB: UPDATE products<br/>SET quantity = quantity - ?<br/>WHERE product_id = ?
        DB-->>ProductRepo: success
        ProductRepo-->>Service: void
        deactivate ProductRepo
    end

    Note over Service: 3. 쿠폰 검증 및 할인 계산
    opt 쿠폰 사용
        loop 각 쿠폰
            Service->>CouponRepo: findUserCouponById(userCouponId)
            activate CouponRepo
            CouponRepo->>DB: SELECT * FROM user_coupons<br/>WHERE user_coupon_id = ?
            DB-->>CouponRepo: user_coupon
            CouponRepo-->>Service: UserCoupon
            deactivate CouponRepo

            alt 쿠폰 만료 또는 사용됨
                Service->>ProductRepo: rollbackStock()
                Service-->>Controller: Exception: 쿠폰 유효하지 않음
                Controller-->>Client: 400 Bad Request
            end

            Service->>Service: applyDiscount(price, coupon)
        end
    end

    Note over Service: 4. 최종 금액 계산
    Service->>Service: calculateFinalAmount()

    Note over Service: 5. 포인트 확인 및 차감
    Service->>PointRepo: findByUserIdWithLock(userId)
    activate PointRepo
    PointRepo->>DB: SELECT * FROM points<br/>WHERE user_id = ?<br/>FOR UPDATE
    DB-->>PointRepo: point
    PointRepo-->>Service: Point
    deactivate PointRepo

    alt 포인트 부족
        Service->>ProductRepo: rollbackStock()
        Service-->>Controller: Exception: 포인트 부족
        Controller-->>Client: 409 Conflict
    end

    Service->>PointRepo: deductPoints(userId, amount)
    activate PointRepo
    PointRepo->>DB: UPDATE points<br/>SET amount = amount - ?<br/>WHERE user_id = ?
    DB-->>PointRepo: success
    PointRepo-->>Service: void
    deactivate PointRepo

    Service->>PointRepo: createTransaction(pointId, -amount)
    activate PointRepo
    PointRepo->>DB: INSERT INTO point_transactions<br/>(point_id, amount, created_at)
    DB-->>PointRepo: success
    PointRepo-->>Service: PointTransaction
    deactivate PointRepo

    Note over Service: 6. 주문 생성
    Service->>OrderRepo: createOrderItems(orderItems)
    activate OrderRepo
    OrderRepo->>DB: INSERT INTO order_items<br/>(user_id, product_id, ...)
    DB-->>OrderRepo: order_items
    OrderRepo-->>Service: List<OrderItem>
    deactivate OrderRepo

    Note over Service: 7. 쿠폰 사용 처리
    opt 쿠폰 사용
        loop 각 쿠폰
            Service->>CouponRepo: markAsUsed(userCouponId)
            activate CouponRepo
            CouponRepo->>DB: UPDATE user_coupons<br/>SET used_at = NOW()<br/>WHERE user_coupon_id = ?
            DB-->>CouponRepo: success
            CouponRepo-->>Service: void
            deactivate CouponRepo
        end
    end

    Note over Service: 8. 장바구니 비우기
    Service->>CartRepo: deleteByUserId(userId)
    activate CartRepo
    CartRepo->>DB: DELETE FROM cart_items<br/>WHERE user_id = ?
    DB-->>CartRepo: success
    CartRepo-->>Service: void
    deactivate CartRepo

    Service-->>Controller: OrderResponse
    deactivate Service

    Controller-->>Client: 201 Created<br/>{orderItems, totalAmount, ...}
    deactivate Controller

    Note over Service,ExternalAPI: 9. 외부 시스템 전송 (비동기)<br/>주문 성공과 무관하게 별도 처리
    Service--)ExternalAPI: sendOrderData(orderData)
    Note right of ExternalAPI: 비동기 전송<br/>성공/실패 여부와<br/>무관하게 주문 완료
```

### 2. 쿠폰 발급 시퀀스

```mermaid
sequenceDiagram
    actor Client
    participant Controller as CouponController
    participant Service as CouponService
    participant CouponRepo as CouponRepository
    participant UserCouponRepo as UserCouponRepository
    participant DB as Database

    Client->>Controller: POST /coupons/{couponId}/issue<br/>{userId}
    activate Controller

    Controller->>Service: issueCoupon(couponId, userId)
    activate Service

    Note over Service,DB: 트랜잭션 시작 (@Transactional)

    Note over Service: 1. 쿠폰 조회
    Service->>CouponRepo: findById(couponId)
    activate CouponRepo
    CouponRepo->>DB: SELECT * FROM coupons<br/>WHERE coupon_id = ?
    DB-->>CouponRepo: coupon
    CouponRepo-->>Service: Coupon
    deactivate CouponRepo

    alt 쿠폰이 존재하지 않음
        Service-->>Controller: Exception: 쿠폰을 찾을 수 없음
        Note over Service,DB: 트랜잭션 롤백
        Controller-->>Client: 404 Not Found
    end

    Note over Service: 2. 중복 발급 확인
    Service->>UserCouponRepo: existsByUserIdAndCouponId(userId, couponId)
    activate UserCouponRepo
    UserCouponRepo->>DB: SELECT COUNT(*) FROM user_coupons<br/>WHERE user_id = ? AND coupon_id = ?
    DB-->>UserCouponRepo: count
    UserCouponRepo-->>Service: boolean
    deactivate UserCouponRepo

    alt 이미 발급받음
        Service-->>Controller: Exception: 이미 발급받은 쿠폰
        Note over Service,DB: 트랜잭션 롤백
        Controller-->>Client: 409 Conflict
    end

    Note over Service: 3. 수량 확인
    Service->>Service: check if issuedQuantity < totalQuantity

    alt 쿠폰 소진
        Service-->>Controller: Exception: 쿠폰 소진
        Note over Service,DB: 트랜잭션 롤백
        Controller-->>Client: 409 Conflict
    end

    Note over Service: 4. 수량 감소 (동시성 제어 없음)
    Service->>CouponRepo: decreaseQuantity(couponId)
    activate CouponRepo
    CouponRepo->>DB: UPDATE coupons<br/>SET total_quantity = total_quantity - 1<br/>WHERE coupon_id = ?
    Note right of DB: ⚠️ Race Condition 발생 가능<br/>동시 요청 시 수량 오류
    DB-->>CouponRepo: success
    CouponRepo-->>Service: void
    deactivate CouponRepo

    Note over Service: 5. 만료일시 계산
    Service->>Service: calculateExpiredAt()<br/>(issuedAt + validDays)

    Note over Service: 6. 사용자 쿠폰 생성
    Service->>UserCouponRepo: create(userCoupon)
    activate UserCouponRepo
    UserCouponRepo->>DB: INSERT INTO user_coupons<br/>(user_id, coupon_id, issued_at, expired_at)
    DB-->>UserCouponRepo: user_coupon
    UserCouponRepo-->>Service: UserCoupon
    deactivate UserCouponRepo

    Note over Service,DB: 트랜잭션 커밋

    Service-->>Controller: UserCouponResponse
    deactivate Service

    Controller-->>Client: 201 Created<br/>{userCouponId, couponName, expiredAt, ...}
    deactivate Controller
```

### 3. 즉시 구매 시퀀스 (장바구니 없이)

```mermaid
sequenceDiagram
    actor Client
    participant Controller as OrderController
    participant Service as OrderService
    participant ProductRepo as ProductRepository
    participant CouponRepo as CouponRepository
    participant PointRepo as PointRepository
    participant OrderRepo as OrderRepository
    participant ExternalAPI as 외부 시스템
    participant DB as Database

    Client->>Controller: POST /orders/direct<br/>{userId, productId, quantity, userCouponId}
    activate Controller

    Controller->>Service: createDirectOrder(request)
    activate Service

    Note over Service: 1. 상품 조회 및 검증
    Service->>ProductRepo: findById(productId)
    activate ProductRepo
    ProductRepo->>DB: SELECT * FROM products<br/>WHERE product_id = ?
    DB-->>ProductRepo: product
    ProductRepo-->>Service: Product
    deactivate ProductRepo

    alt 상품이 존재하지 않음
        Service-->>Controller: Exception: 상품을 찾을 수 없음
        Controller-->>Client: 404 Not Found
    end

    Note over Service: 2. 재고 확인 및 차감
    Service->>ProductRepo: findByIdWithLock(productId)
    activate ProductRepo
    ProductRepo->>DB: SELECT * FROM products<br/>WHERE product_id = ?<br/>FOR UPDATE
    DB-->>ProductRepo: product (LOCKED)
    ProductRepo-->>Service: Product
    deactivate ProductRepo

    Service->>Service: checkStock(product, quantity)

    alt 재고 부족
        Service-->>Controller: Exception: 재고 부족
        Controller-->>Client: 409 Conflict
    end

    Service->>ProductRepo: decreaseStock(productId, quantity)
    activate ProductRepo
    ProductRepo->>DB: UPDATE products<br/>SET quantity = quantity - ?<br/>WHERE product_id = ?
    DB-->>ProductRepo: success
    ProductRepo-->>Service: void
    deactivate ProductRepo

    Note over Service: 3. 주문 금액 계산
    Service->>Service: calculateTotalPrice(product, quantity)

    Note over Service: 4. 쿠폰 검증 및 할인 계산
    opt 쿠폰 사용
        Service->>CouponRepo: findUserCouponById(userCouponId)
        activate CouponRepo
        CouponRepo->>DB: SELECT * FROM user_coupons<br/>WHERE user_coupon_id = ?
        DB-->>CouponRepo: user_coupon
        CouponRepo-->>Service: UserCoupon
        deactivate CouponRepo

        alt 쿠폰 만료 또는 사용됨
            Service->>ProductRepo: rollbackStock(productId, quantity)
            Service-->>Controller: Exception: 쿠폰 유효하지 않음
            Controller-->>Client: 400 Bad Request
        end

        Service->>Service: applyDiscount(totalPrice, coupon)
    end

    Note over Service: 5. 최종 금액 계산
    Service->>Service: calculateFinalAmount()

    Note over Service: 6. 포인트 확인 및 차감
    Service->>PointRepo: findByUserIdWithLock(userId)
    activate PointRepo
    PointRepo->>DB: SELECT * FROM points<br/>WHERE user_id = ?<br/>FOR UPDATE
    DB-->>PointRepo: point (LOCKED)
    PointRepo-->>Service: Point
    deactivate PointRepo

    alt 포인트 부족
        Service->>ProductRepo: rollbackStock(productId, quantity)
        Service-->>Controller: Exception: 포인트 부족
        Controller-->>Client: 409 Conflict
    end

    Service->>PointRepo: deductPoints(userId, finalAmount)
    activate PointRepo
    PointRepo->>DB: UPDATE points<br/>SET amount = amount - ?<br/>WHERE user_id = ?
    DB-->>PointRepo: success
    PointRepo-->>Service: void
    deactivate PointRepo

    Service->>PointRepo: createTransaction(pointId, -finalAmount)
    activate PointRepo
    PointRepo->>DB: INSERT INTO point_transactions<br/>(point_id, amount, created_at)
    DB-->>PointRepo: success
    PointRepo-->>Service: PointTransaction
    deactivate PointRepo

    Note over Service: 7. 주문 생성
    Service->>OrderRepo: createOrderItem(orderItem)
    activate OrderRepo
    OrderRepo->>DB: INSERT INTO order_items<br/>(user_id, product_id, quantity,<br/>total_price, discount_amount,<br/>final_amount, point_transaction_id, ...)
    DB-->>OrderRepo: order_item
    OrderRepo-->>Service: OrderItem
    deactivate OrderRepo

    Note over Service: 8. 쿠폰 사용 처리
    opt 쿠폰 사용
        Service->>CouponRepo: markAsUsed(userCouponId)
        activate CouponRepo
        CouponRepo->>DB: UPDATE user_coupons<br/>SET used_at = NOW()<br/>WHERE user_coupon_id = ?
        DB-->>CouponRepo: success
        CouponRepo-->>Service: void
        deactivate CouponRepo
    end

    Service-->>Controller: OrderResponse
    deactivate Service

    Controller-->>Client: 201 Created<br/>{orderItem, totalAmount, finalAmount, ...}
    deactivate Controller

    Note over Service,ExternalAPI: 9. 외부 시스템 전송 (비동기)<br/>주문 성공과 무관하게 별도 처리
    Service--)ExternalAPI: sendOrderData(orderData)
    Note right of ExternalAPI: 비동기 전송<br/>성공/실패 여부와<br/>무관하게 주문 완료
```
