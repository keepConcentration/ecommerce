# API 명세서 (API Specification)

## 목차
1. [공통 사항](#공통-사항)
2. [상품 API](#상품-api)
3. [장바구니 API](#장바구니-api)
4. [주문/결제 API](#주문결제-api)
5. [쿠폰 API](#쿠폰-api)
6. [포인트 API](#포인트-api)
7. [에러 코드](#에러-코드)

---

## 공통 사항

### Base URL
```
http://localhost:8080/api/v1
```

### 공통 헤더
```http
Content-Type: application/json
```

### 공통 응답 형식

#### 성공 응답
```json
{
  "status": true,
  "data": { ... },
  "error": null
}
```

#### 실패 응답
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지"
  }
}
```

### HTTP 상태 코드
- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공
- `204 No Content`: 응답 콘텐츠 제공하지 않음.
- `400 Bad Request`: 잘못된 요청
- `404 Not Found`: 리소스를 찾을 수 없음
- `409 Conflict`: 비즈니스 규칙 위반 (재고 부족, 쿠폰 소진 등)
- `500 Internal Server Error`: 서버 내부 오류

---

## 상품 API

### 1. 상품 목록 조회
상품 전체 목록을 조회합니다.

**Endpoint**
```
GET /products
```

**Request**
```http
GET /api/v1/products
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": [
    {
      "productId": 1,
      "name": "노트북",
      "price": 1500000,
      "quantity": 50,
      "createdAt": "2025-01-15T10:00:00",
      "updatedAt": "2025-01-20T15:30:00"
    },
    {
      "productId": 2,
      "name": "마우스",
      "price": 35000,
      "quantity": 0,
      "createdAt": "2025-01-15T10:00:00",
      "updatedAt": "2025-01-20T15:30:00"
    }
  ],
  "error": null
}
```

---

### 2. 상품 상세 조회
특정 상품의 상세 정보를 조회합니다.

**Endpoint**
```
GET /products/{productId}
```

**Path Parameters**

| Parameter  | Type  | Required | Description  |
|:-----------|:------|:--------:|:-------------|
| productId  | Long  |   Yes    | 상품 ID        |


**Request**
```http
GET /api/v1/products/1
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": {
    "productId": 1,
    "name": "노트북",
    "price": 1500000,
    "quantity": 50,
    "createdAt": "2025-01-15T10:00:00",
    "updatedAt": "2025-01-20T15:30:00"
  },
  "error": null
}
```

**Response** (404 Not Found)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품이 존재하지 않습니다"
  }
}
```

---

### 3. 인기 상품 조회 (Top 5)
최근 3일간 판매량 기준 상위 5개 상품을 조회합니다.

**Endpoint**
```
GET /products/popular
```

**Request**
```http
GET /api/v1/products/popular
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": [
    {
      "productId": 1,
      "name": "노트북",
      "price": 1500000,
      "totalSales": 150
    },
    {
      "productId": 3,
      "name": "키보드",
      "price": 120000,
      "totalSales": 98
    },
    {
      "productId": 2,
      "name": "마우스",
      "price": 35000,
      "totalSales": 75
    }
  ],
  "error": null
}
```

---

## 장바구니 API

### 1. 장바구니 상품 추가
장바구니에 상품을 추가하거나 수량을 증가시킵니다.

**Endpoint**
```
POST /cart/items
```

**Request Body**

| Field     | Type | Required | Description |
|:----------|:-----|:--------:|:------------|
| userId    | Long |   Yes    | 사용자 ID      |
| productId | Long |   Yes    | 상품 ID       |
| quantity  | Long |   Yes    | 수량 (1 이상)   |

**Request**
```http
PUT /api/v1/cart/items
Content-Type: application/json

{
  "userId": 1,
  "productId": 1,
  "quantity": 3
}
```

**Response** (201 Created)  

header:
```json
Location: /api/v1/cart/items/1
```

body: 
```json
{
  "status": true,
  "data": {
    "cartItemId": 1,
    "productId": 1,
    "productName": "노트북",
    "price": 1500000,
    "quantity": 3
  },
  "error": null
}
```

**Response** (404 Not Found)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품이 존재하지 않습니다"
  }
}
```

---

### 2. 장바구니 조회
사용자의 장바구니 목록을 조회합니다.

**Endpoint**
```
GET /cart/items?userId={userId}
```

**Parameters**

| Parameter | Type | Required  | Description |
|:----------|:-----|:---------:|:------------|
| userId    | Long |    Yes    | 사용자 ID      |

**Request**
```http
GET /cart/items?userId=1
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": {
    "items": [
      {
        "cartItemId": 1,
        "productId": 1,
        "productName": "노트북",
        "price": 1500000,
        "quantity": 2
      },
      {
        "cartItemId": 2,
        "productId": 2,
        "productName": "마우스",
        "price": 35000,
        "quantity": 1
      }
    ]
  },
  "error": null
}
```

---

### 3. 장바구니 수량 변경
장바구니 상품의 수량을 변경합니다.

**Endpoint**
```
PATCH /cart/items/{cartItemId}
```

**Path Parameters**

| Parameter  | Type | Required | Description |
|:-----------|:-----|:--------:|:------------|
| cartItemId | Long |   Yes    | 장바구니 아이템 ID |

**Request Body**

| Field    | Type | Required | Description |
|:---------|:-----|:--------:|:------------|
| quantity | Long |   Yes    | 변경할 수량 (1 이상) |

**Request**
```http
PATCH /api/v1/cart/items/1
Content-Type: application/json

{
  "quantity": 5
}
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": {
    "cartItemId": 1,
    "productId": 1,
    "productName": "노트북",
    "price": 1500000,
    "quantity": 5
  },
  "error": null
}
```

**Response** (404 Not Found)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "CART_ITEM_NOT_FOUND",
    "message": "장바구니 아이템이 존재하지 않습니다"
  }
}
```

**Response** (400 Bad Request)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "INVALID_QUANTITY",
    "message": "유효하지 않은 수량입니다"
  }
}
```

---

### 4. 장바구니 상품 삭제
장바구니에서 특정 상품을 삭제합니다.

**Endpoint**
```
DELETE /cart/items/{cartItemId}
```

**Path Parameters**

| Parameter  | Type | Required | Description |
|:-----------|:-----|:--------:|:------------|
| cartItemId | Long |   Yes    | 장바구니 아이템 ID |

**Request**
```http
DELETE /api/v1/cart/items/1
```

**Response** (204 No Content)
```
(응답 본문 없음)
```

**Response** (404 Not Found)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "CART_ITEM_NOT_FOUND",
    "message": "장바구니 아이템이 존재하지 않습니다"
  }
}
```

---

## 주문/결제 API

### 1. 주문 생성 및 결제
장바구니의 상품들로 주문을 생성하고 포인트로 결제를 처리합니다. (쿠폰 선택적 적용)

**Endpoint**
```
POST /orders
```

**Request Body**

| Field              | Type                            | Required | Description |
|:-------------------|:--------------------------------|:--------:|:------------|
| userId             | Long                            |   Yes    | 사용자 ID      |
| cartItemCouponMaps | List&lt;CartItemCouponMap&gt; |    No    | 장바구니 아이템별 쿠폰 매핑 |

**CartItemCouponMap**

| Field        | Type | Required | Description  |
|:-------------|:-----|:--------:|:-------------|
| cartItemId   | Long |   Yes    | 장바구니 아이템 ID  |
| userCouponId | Long |   Yes    | 사용할 쿠폰 ID    |

**Request**
```http
POST /api/v1/orders
Content-Type: application/json

{
  "userId": 1,
  "cartItemCouponMaps": [
    {
      "cartItemId": 1,
      "userCouponId": 10
    },
    {
      "cartItemId": 2,
      "userCouponId": 11
    }
  ]
}
```

**Response** (201 Created)
```json
{
  "status": true,
  "data": {
    "orderItems": [
      {
        "orderItemId": 1,
        "productId": 1,
        "productName": "노트북",
        "quantity": 2,
        "price": 1500000,
        "totalPrice": 3000000,
        "discountAmount": 50000,
        "finalAmount": 2950000,
        "userCouponId": 10
      },
      {
        "orderItemId": 2,
        "productId": 2,
        "productName": "마우스",
        "quantity": 1,
        "price": 35000,
        "totalPrice": 35000,
        "discountAmount": 5000,
        "finalAmount": 30000,
        "userCouponId": 11
      }
    ]
  },
  "error": null
}
```

**Response** (409 Conflict - 재고 부족)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "재고가 부족합니다"
  }
}
```

**Response** (409 Conflict - 포인트 부족)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "INSUFFICIENT_POINTS",
    "message": "포인트 잔액이 부족합니다"
  }
}
```

**Response** (400 Bad Request - 만료된 쿠폰)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "COUPON_EXPIRED",
    "message": "만료된 쿠폰입니다"
  }
}
```

**Response** (400 Bad Request - 이미 사용된 쿠폰)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "COUPON_ALREADY_USED",
    "message": "이미 사용된 쿠폰입니다"
  }
}
```

---

## 쿠폰 API

### 1. 쿠폰 발급
선착순 쿠폰을 발급받습니다.

**Endpoint**
```
POST /coupons/{couponId}/issue
```

**Path Parameters**

| Parameter | Type  | Required | Description |
|:----------|:-----:|:--------:|:------------|
| couponId  | Long  |   Yes    | 쿠폰 ID       |

**Request Body**

| Field  | Type   | Required  | Description  |
|:-------|:-------|:---------:|:-------------|
| userId | Long   |    Yes    | 사용자 ID       |

**Request**
```http
POST /api/v1/coupons/1/issue
Content-Type: application/json

{
  "userId": 1
}
```

**Response** (201 Created)
```json
{
  "status": true,
  "data": {
    "userCouponId": 10,
    "userId": 1,
    "couponId": 1,
    "couponName": "신규 가입 50000원 할인",
    "discountAmount": 50000,
    "issuedAt": "2025-01-20T10:00:00",
    "expiredAt": "2025-01-27T23:59:59",
    "validDays": 7
  },
  "error": null
}
```

**Response** (409 Conflict - 쿠폰 소진)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "COUPON_SOLD_OUT",
    "message": "쿠폰이 모두 소진되었습니다"
  }
}
```

**Response** (409 Conflict - 중복 발급)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "COUPON_ALREADY_ISSUED",
    "message": "이미 발급받은 쿠폰입니다"
  }
}
```

---

### 2. 보유 쿠폰 조회
사용 가능한 쿠폰 목록을 조회합니다. (미사용 + 미만료)

**Endpoint**
```
GET /coupons?userId={userId}
```

**Parameters**

| Parameter | Type  | Required | Description  |
|:----------|:------|:--------:|:-------------|
| userId    | Long  |   Yes    | 사용자 ID       |

**Request**
```http
GET /api/v1/coupons?userId=1
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": [
    {
      "userCouponId": 11,
      "couponId": 2,
      "couponName": "10000원 할인",
      "discountAmount": 10000,
      "issuedAt": "2025-01-18T10:00:00",
      "expiredAt": "2025-01-21T23:59:59"
    },
    {
      "userCouponId": 10,
      "couponId": 1,
      "couponName": "50000원 할인",
      "discountAmount": 50000,
      "issuedAt": "2025-01-15T10:00:00",
      "expiredAt": "2025-01-22T23:59:59"
    }
  ],
  "error": null
}
```

---

## 포인트 API

### 1. 포인트 잔액 조회
사용자의 현재 포인트 잔액을 조회합니다.

**Endpoint**
```
GET /points?userId={userId}
```

**Parameters**

| Parameter | Type  |  Required  | Description  |
|:----------|:------|:----------:|:-------------|
| userId    | Long  |    Yes     | 사용자 ID       |

**Request**
```http
GET /api/v1/points?userId=1
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": {
    "pointId": 1,
    "userId": 1,
    "amount": 50000,
    "updatedAt": "2025-01-20T10:00:00"
  },
  "error": null
}
```

---

### 2. 포인트 충전
포인트를 충전합니다.

**Endpoint**
```
POST /points/charge
```

**Request Body**

| Field   | Type  |  Required  | Description  |
|:--------|:------|:----------:|:-------------|
| userId  | Long  |    Yes     | 사용자 ID       |
| amount  | Long  |    Yes     | 충전 금액 (1 이상) |

**Request**
```http
POST /api/v1/points/charge
Content-Type: application/json

{
  "amount": 100000,
  "userId": 1
}
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": {
    "pointId": 1,
    "userId": 1,
    "amount": 150000,
    "chargedAmount": 100000,
    "pointTransactionId": 123,
    "createdAt": "2025-01-20T15:00:00"
  },
  "error": null
}
```

**Response** (400 Bad Request)
```json
{
  "status": false,
  "data": null,
  "error": {
    "code": "INVALID_AMOUNT",
    "message": "유효하지 않은 금액입니다"
  }
}
```

---

### 3. 포인트 거래 내역 조회
포인트 충전/사용 내역을 조회합니다.

**Endpoint**
```
GET /transactions?userId={userId}
```

**Parameters**

| Parameter | Type  |  Required  | Description  |
|:----------|:------|:----------:|:-------------|
| userId    | Long  |    Yes     | 사용자 ID       |

**Request**
```http
GET /api/v1/transactions?userId={userId}
```

**Response** (200 OK)
```json
{
  "status": true,
  "data": [
    {
      "pointTransactionId": 125,
      "pointId": 1,
      "amount": -2980000,
      "createdAt": "2025-01-20T15:30:00"
    },
    {
      "pointTransactionId": 124,
      "pointId": 1,
      "amount": 100000,
      "createdAt": "2025-01-20T15:00:00"
    },
    {
      "pointTransactionId": 123,
      "pointId": 1,
      "amount": 50000,
      "createdAt": "2025-01-20T10:00:00"
    }
  ],
  "error": null
}
```

---

## 에러 코드

| 에러 코드 | HTTP 상태 | 설명 |
|----------|-----------|------|
| `PRODUCT_NOT_FOUND` | 404 | 상품을 찾을 수 없음 |
| `CART_ITEM_NOT_FOUND` | 404 | 장바구니 아이템을 찾을 수 없음 |
| `ORDER_NOT_FOUND` | 404 | 주문을 찾을 수 없음 |
| `USER_NOT_FOUND` | 404 | 사용자를 찾을 수 없음 |
| `COUPON_NOT_FOUND` | 404 | 쿠폰을 찾을 수 없음 |
| `INSUFFICIENT_STOCK` | 409 | 재고 부족 |
| `INSUFFICIENT_POINTS` | 409 | 포인트 잔액 부족 |
| `COUPON_SOLD_OUT` | 409 | 쿠폰 소진 |
| `COUPON_ALREADY_ISSUED` | 409 | 이미 발급받은 쿠폰 |
| `COUPON_EXPIRED` | 400 | 만료된 쿠폰 |
| `COUPON_ALREADY_USED` | 400 | 이미 사용된 쿠폰 |
| `INVALID_AMOUNT` | 400 | 유효하지 않은 금액 |
| `INVALID_QUANTITY` | 400 | 유효하지 않은 수량 |
| `INVALID_REQUEST` | 400 | 잘못된 요청 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 오류 |
