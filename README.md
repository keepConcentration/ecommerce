# E-commerce Platform

포인트 기반 결제 시스템, 쿠폰 관리, 장바구니 기능을 갖춘 이커머스 플랫폼입니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Database | MySQL 8.0 |
| Cache | Redis 7 |
| ORM | Spring Data JPA |
| Build | Gradle 8.x |
| Container | Docker, Docker Compose |
| Load Balancer | Nginx |
| Test | JUnit 5, Testcontainers |
| API Docs | SpringDoc OpenAPI (Swagger) |

## 프로젝트 구조

```
ecommerce/
├── api-server/                 # Spring Boot 애플리케이션
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/phm/ecommerce/
│   │   │   │   ├── presentation/       # API 계층 (Controller, DTO)
│   │   │   │   ├── application/        # 애플리케이션 계층 (UseCase)
│   │   │   │   ├── domain/             # 도메인 계층 (Entity, Exception)
│   │   │   │   ├── infrastructure/     # 인프라 계층 (Repository)
│   │   │   │   └── config/             # 설정
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/                       # 테스트 코드
│   ├── build.gradle
│   ├── Dockerfile
│   └── gradlew
├── nginx/                      # Nginx 로드밸런서 설정
├── redis/                      # Redis 설정
├── docs/                       # 문서
│   ├── api/                    # API 명세, 요구사항
├── docker-compose.yml
├── Makefile
```

## 요구 사항

- **Java 21** 이상
- **Docker** & **Docker Compose** (컨테이너 실행 시)
- **MySQL 8.0** (로컬 실행 시)

## 빠른 시작

### 방법 1: Docker Compose (권장)

모든 서비스를 한 번에 실행합니다.

```bash
# 서비스 시작 (API 서버 3개 인스턴스)
make up

# 또는 직접 실행
docker-compose up -d --scale api-server=3
```

### 방법 2: 로컬 개발 환경

```bash
# 1. MySQL 실행 (Docker)
docker-compose up -d mysql

# 2. API 서버 빌드 및 실행
cd api-server
./gradlew bootRun
```

## 빌드 및 실행

### 빌드

```bash
cd api-server
./gradlew build
```

### 테스트

```bash
# 전체 테스트 실행 (Docker 필요 - Testcontainers 사용)
cd api-server
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "CartIntegrationTest"

# 특정 패턴의 테스트 실행
./gradlew test --tests "*Concurrency*"
```

### 애플리케이션 실행

```bash
cd api-server
./gradlew bootRun
```

## API 접근

| 환경 | URL | 설명 |
|------|-----|------|
| 로컬 개발 | http://localhost:8085 | API 서버 직접 실행 (`./gradlew bootRun`) |
| Docker | http://localhost:8085 | Nginx → API 서버 (로드밸런싱) |
| Swagger UI | http://localhost:8085/swagger-ui.html | API 문서 |
| OpenAPI | http://localhost:8085/api-docs | OpenAPI JSON |

> 로컬과 Docker 환경 모두 8085 포트를 사용하므로 동시에 실행할 수 없습니다.

## Make 명령어

| 명령어 | 설명 |
|--------|------|
| `make build` | Docker 이미지 빌드 |
| `make up` | 서비스 시작 (API 서버 3개) |
| `make up-build` | 빌드 후 서비스 시작 |
| `make up-scale N=5` | API 서버 N개로 스케일 |
| `make down` | 서비스 중지 |
| `make down-v` | 서비스 중지 + 볼륨 삭제 |
| `make logs` | 전체 로그 확인 |
| `make logs-api` | API 서버 로그 |
| `make test` | 테스트 실행 |
| `make clean` | 빌드 아티팩트 정리 |
| `make health` | 서비스 상태 확인 |
| `make seed` | 테스트 데이터 생성 |
| `make reset-db` | MySQL 데이터 전체 삭제 |

## 환경 설정

### 포트 구성

| 서비스 | 내부 포트 | 외부 포트 |
|--------|-----------|-----------|
| Nginx | 80 | 8085 |
| API Server | 8080 | - |
| MySQL | 3306 | 3306 |
| Redis | 6379 | 6379 |

## 아키텍처

### 계층 구조

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│         (Controller, DTO, API Docs)         │
├─────────────────────────────────────────────┤
│              Application Layer              │
│           (UseCase, LockManager)            │
├─────────────────────────────────────────────┤
│                Domain Layer                 │
│        (Entity, Domain Service, Exception)  │
├─────────────────────────────────────────────┤
│             Infrastructure Layer            │
│            (JPA Repository, DB)             │
└─────────────────────────────────────────────┘
```

### 주요 도메인

- **Product**: 상품 관리, 재고, 인기 상품 조회
- **Cart**: 장바구니 관리
- **Order**: 주문 생성 (장바구니/즉시 구매)
- **Coupon**: 쿠폰 발급 및 사용
- **Point**: 포인트 충전 및 결제

### 동시성 제어

| 도메인 | 전략 | 설명 |
|--------|------|------|
| Coupon | LockManager | ReentrantLock 기반 동시성 제어 |
| Product | Optimistic Lock + @Retryable | 재고 차감 시 낙관적 락 |
| Point | Optimistic Lock + @Retryable | 포인트 충전/차감 시 낙관적 락 |

## 테스트

### 테스트 유형

- **통합 테스트**: `@SpringBootTest` + Testcontainers (실제 MySQL)
- **도메인 테스트**: 엔티티 및 도메인 서비스 단위 테스트
- **동시성 테스트**: 멀티스레드 환경 동시성 검증

### 커버리지

- Jacoco 사용, 70% 라인 커버리지 기준
- 리포트: `api-server/build/jacocoHtml/index.html`

## 테스트 데이터 생성 (Seeding)

대량의 테스트 데이터를 생성하여 성능 테스트 등에 활용할 수 있습니다.

### 실행 방법

```bash
# MySQL이 실행 중인 상태에서
make seed

# 또는 직접 실행
cd api-server && ./gradlew seedData
```

### 생성되는 데이터

| 테이블 | 건수 | 설명 |
|--------|------|------|
| users | 100,000 | |
| products | 10,000 | |
| coupons | 1,000 | |
| user_coupons | ~500,000 | 중복 제외 (INSERT IGNORE) |
| cart_items | ~200,000 | 중복 제외 (INSERT IGNORE) |
| points | 100,000 | 유저당 1개 (1:1 관계) |
| orders | 500,000 | |
| order_items | ~1,000,000 | 주문당 1~3개 |
| point_transactions | ~650,000 | 주문 차감 500,000 + 충전 ~150,000 |

**예상 용량: 약 2~3GB** (인덱스 포함)

### 데이터 정합성

- **Point 잔액** = PointTransaction 합계
- **Order 금액** = OrderItem 합계
- **PointTransaction 차감 금액** = Order.finalAmount
- **모든 Point 잔액 >= 0** (음수 방지)

### 주의사항

- MySQL이 먼저 실행되어 있어야 합니다
- 기존 데이터가 있으면 중복 키 오류가 발생할 수 있습니다
- 실행 시간: 약 10~30분 소요
- 메모리 사용량: 약 2GB (JVM 힙 설정 권장: `-Xmx4g`)

## 트러블슈팅

### IntelliJ에서 빌드 실패

프로젝트 구조 변경 후 IntelliJ에서 인식하지 못하는 경우:

1. `File` → `Open` → `api-server` 폴더 선택
2. 또는 `File` → `Invalidate Caches...` → `Invalidate and Restart`

### Testcontainers 실행 실패

```bash
# Docker 실행 확인
docker ps

# Docker가 실행 중이어야 테스트가 정상 동작합니다
```

### 포트 충돌

```bash
# 사용 중인 포트 확인
lsof -i :8085
lsof -i :3306

# 기존 컨테이너 정리
docker-compose down -v
```
