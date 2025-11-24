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
